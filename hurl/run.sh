set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"
ENV_FILE="${ENV_FILE:-../example.env}"
if [[ -f "$ENV_FILE" ]]; then
    while IFS= read -r line || [[ -n "$line" ]]; do
        line="${line%$'\r'}"
        [[ "$line" =~ ^[[:space:]]*$ || "$line" =~ ^[[:space:]]*# ]] && continue
        if [[ ! "$line" =~ ^([A-Za-z_][A-Za-z0-9_]*)=(.*)$ ]]; then
            echo "Некорректная строка в $ENV_FILE" >&2
            exit 2
        fi
        key="${BASH_REMATCH[1]}"
        value="${BASH_REMATCH[2]}"
        if [[ ${#value} -ge 2 ]]; then
            first="${value:0:1}"
            last="${value: -1}"
            if [[ "$first" == '"' && "$last" == '"' || "$first" == "'" && "$last" == "'" ]]; then
                value="${value:1:${#value}-2}"
            fi
        fi
        [[ -v "$key" ]] || export "$key=$value"
    done < "$ENV_FILE"
fi
for key in APP_HOST APP_PORT DB_HOST DB_PORT DB_NAME DB_USER DB_PASSWORD TEST_PASSWORD; do
    if [[ ! -v "$key" ]]; then
        echo "Не задана переменная $key: проверь .env или env_file в Compose" >&2
        exit 2
    fi
done
export PGHOST="$DB_HOST" PGPORT="$DB_PORT" PGDATABASE="$DB_NAME"
export PGUSER="$DB_USER" PGPASSWORD="$DB_PASSWORD"
HURL="${HURL:-hurl}"
PSQL="${PSQL:-psql}"
BASE_URL="http://${APP_HOST}:${APP_PORT}"
TEST_PASSWORD_JSON="${TEST_PASSWORD//\\/\\\\}"
TEST_PASSWORD_JSON="${TEST_PASSWORD_JSON//\"/\\\"}"
TEST_PASSWORD_JSON="${TEST_PASSWORD_JSON//$'\n'/\\n}"
TEST_PASSWORD_JSON="${TEST_PASSWORD_JSON//$'\r'/\\r}"
TEST_PASSWORD_JSON="${TEST_PASSWORD_JSON//$'\t'/\\t}"

usage() {
    echo 'Запуск: bash run.sh [1|2|3|4|5|all|имя.hurl]'
    echo 'Настройки читаются из общего example.env или окружения контейнера.'
    echo 'KEEP_DATA=1 сохраняет тестовые данные; REPORT_DIR задаёт каталог HTML-отчёта.'
}
if [[ "${1:-}" == '-h' || "${1:-}" == '--help' ]]; then usage; exit 0; fi
for tool in "$HURL" "$PSQL"; do
    command -v "$tool" >/dev/null || { echo "Не найдена команда: $tool" >&2; exit 2; }
done
if ! "$PSQL" -X -q -v ON_ERROR_STOP=1 -c \
    'SELECT filled_quantity, initial_quantity, expires_at, queue_sequence, matching_pending FROM orders LIMIT 0; SELECT id FROM exchange_state WHERE id = 1;' >/dev/null; then
    echo 'Не готова БД: проверь подключение и применение simple-matching.sql.' >&2
    exit 2
fi

run_one() (
    set -euo pipefail
    local file="$1" case_no="${1:0:2}"
    case_no="$((10#$case_no))"
    local run_id buyer_username seller_username sec_code second_code empty_code day_code reverse_code work lot_size
    run_id="$(date +%s%N)"
    buyer_username="h${case_no}b_${run_id}"
    seller_username="h${case_no}s_${run_id}"
    sec_code="H${case_no}_${run_id}"
    second_code="${sec_code}_B"
    empty_code="${sec_code}_E"
    day_code="${sec_code}_D"
    reverse_code="${sec_code}_R"
    lot_size=1
    [[ "$case_no" != 1 ]] || lot_size=10
    work="$(mktemp -d)"
    local -a dbvars=(
        -v "buyer_username=$buyer_username" -v "seller_username=$seller_username"
        -v "sec_code=$sec_code" -v "second_code=$second_code"
        -v "empty_code=$empty_code" -v "day_code=$day_code"
        -v "reverse_code=$reverse_code" -v "case_no=$case_no" -v "lot_size=$lot_size"
    )
    finish() {
        local rc=$?
        trap - EXIT
        if [[ "${KEEP_DATA:-0}" != 1 ]]; then
            if ! "$PSQL" -X -q -v ON_ERROR_STOP=1 "${dbvars[@]}" -f sql/cleanup.sql >/dev/null; then
                echo "Не удалось удалить тестовые данные: $buyer_username, $seller_username" >&2
                rc=1
            fi
        else
            echo "Данные сохранены: $buyer_username, $seller_username; инструмент $sec_code"
        fi
        rm -rf -- "$work"
        exit "$rc"
    }
    trap finish EXIT
    cat > "$work/variables.env" <<EOF
base_url=$BASE_URL
buyer_username=$buyer_username
seller_username=$seller_username
sec_code=$sec_code
second_code=$second_code
empty_code=$empty_code
day_code=$day_code
reverse_code=$reverse_code
missing_code=MISSING_${run_id}
EOF
    local -a hurlargs=(--test --jobs 1 --connect-timeout 5 --max-time 15 --variables-file "$work/variables.env" --secret "test_password=$TEST_PASSWORD_JSON")
    if [[ -n "${REPORT_DIR:-}" ]]; then
        mkdir -p -- "$REPORT_DIR"
        hurlargs+=(--report-html "$REPORT_DIR")
    fi
    echo "=== $file ==="
    "$HURL" "${hurlargs[@]}" --to-entry 3 "tests/$file" || exit $?
    "$PSQL" -X -qAt -v ON_ERROR_STOP=1 "${dbvars[@]}" -f sql/fixtures.sql >> "$work/variables.env" || exit $?
    "$HURL" "${hurlargs[@]}" --from-entry 4 "tests/$file" || exit $?
    local buyer_account seller_account
    buyer_account="$(sed -n 's/^buyer_account=//p' "$work/variables.env")"
    seller_account="$(sed -n 's/^seller_account=//p' "$work/variables.env")"
    "$PSQL" -X -q -v ON_ERROR_STOP=1 "${dbvars[@]}" \
        -v "buyer_account=$buyer_account" -v "seller_account=$seller_account" \
        -f sql/verify.sql >/dev/null || exit $?
)

case "${1:-all}" in
    1|01_create_orders.hurl) files=(01_create_orders.hurl) ;;
    2|02_update_cancel.hurl) files=(02_update_cancel.hurl) ;;
    3|03_execution.hurl) files=(03_execution.hurl) ;;
    4|04_orderbook.hurl) files=(04_orderbook.hurl) ;;
    5|05_registries.hurl) files=(05_registries.hurl) ;;
    all) files=(01_create_orders.hurl 02_update_cancel.hurl 03_execution.hurl 04_orderbook.hurl 05_registries.hurl) ;;
    *) usage; exit 2 ;;
esac
failed=0
for file in "${files[@]}"; do
    if run_one "$file"; then echo "PASS: $file"; else echo "FAIL: $file" >&2; failed=$((failed + 1)); fi
done
echo "Всего: ${#files[@]}; успешно: $((${#files[@]} - failed)); ошибок: $failed"
[[ "$failed" == 0 ]]
