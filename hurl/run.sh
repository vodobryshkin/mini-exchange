#!/bin/bash
set -e
cd "$(dirname "$0")"

export PGHOST="$DB_HOST" PGPORT="$DB_PORT" PGDATABASE="$DB_NAME"
export PGUSER="$DB_USER" PGPASSWORD="$DB_PASSWORD"

password="${TEST_PASSWORD//\\/\\\\}"
password="${password//\"/\\\"}"
password="${password//$'\n'/\\n}"
password="${password//$'\r'/\\r}"
password="${password//$'\t'/\\t}"

sql() {
    psql -X -qAt -v ON_ERROR_STOP=1 -v "case_no=$number" -f "sql/$1"
}

run_hurl() {
    hurl --test --connect-timeout 5 --max-time 15 --secret "test_password=$password" \
        --variable "base_url=http://$APP_HOST:$APP_PORT/api/v1" --variables-file "/tmp/hurl-$number.env" "$@"
}

case "${1:-all}" in
    all) files="tests/*.hurl" ;;
    [1-5]) files="tests/0${1}_*.hurl" ;;
    *) echo "Запуск: run.sh [1|2|3|4|5|all]"; exit 1 ;;
esac

for file in $files; do
    number=$(basename "$file" | cut -c 2)
    echo "=== $file ==="
    sql cleanup.sql > /dev/null
    sql variables.sql > "/tmp/hurl-$number.env"
    run_hurl --to-entry 3 "$file"
    sql fixtures.sql > "/tmp/hurl-$number.env"
    run_hurl --from-entry 4 "$file"
    sql verify.sql > /dev/null
    if [ "${KEEP_DATA:-0}" != 1 ]; then sql cleanup.sql > /dev/null; fi
    rm "/tmp/hurl-$number.env"
    echo "PASS: $file"
done
