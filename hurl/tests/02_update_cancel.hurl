# Редактирование и отмена

# Получение CSRF перед регистрацией или входом
GET {{base_url}}/auth/csrf
HTTP 200
[Captures]
csrf: jsonpath "$.token"
[Asserts]
jsonpath "$.headerName" == "X-CSRF-TOKEN"

# Регистрация покупателя
POST {{base_url}}/auth/register
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"username":"{{buyer_username}}","password":"{{test_password}}"}
HTTP 201
[Asserts]
jsonpath "$.message" == "OK"

# Регистрация продавца
POST {{base_url}}/auth/register
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"username":"{{seller_username}}","password":"{{test_password}}"}
HTTP 201
[Asserts]
jsonpath "$.message" == "OK"

# Получение CSRF перед регистрацией или входом
GET {{base_url}}/auth/csrf
HTTP 200
[Captures]
csrf: jsonpath "$.token"
[Asserts]
jsonpath "$.headerName" == "X-CSRF-TOKEN"

# Вход покупателя
POST {{base_url}}/auth/login
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"username":"{{buyer_username}}","password":"{{test_password}}"}
HTTP 200
[Asserts]
jsonpath "$.message" == "OK"

# Обновление CSRF после входа
GET {{base_url}}/auth/csrf
HTTP 200
[Captures]
csrf: jsonpath "$.token"
[Asserts]
jsonpath "$.headerName" == "X-CSRF-TOKEN"

# Проверка текущего пользователя
GET {{base_url}}/auth/me
HTTP 200
[Asserts]
jsonpath "$.username" == "{{buyer_username}}"

# Создание заявки для последующего редактирования
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":10}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Сохранение идентификатора заявки и её места в очереди
GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
order_id: jsonpath "$.items[0].id"
old_sequence: jsonpath "$.items[0].queue_sequence"
[Asserts]
jsonpath "$.total_elements" == 1

# Изменение цены и увеличение количества: заявка теряет приоритет
PUT {{base_url}}/orders/{{order_id}}
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"time_in_force":"DAY","price":101,"quantity":12}
HTTP 200
[Captures]
new_sequence: jsonpath "$.queue_sequence"
[Asserts]
jsonpath "$.price" == 101
jsonpath "$.quantity" == 12
jsonpath "$.initial_quantity" == 10
jsonpath "$.queue_sequence" > {{old_sequence}}

# Уменьшение количества при прежней цене: приоритет сохраняется
PUT {{base_url}}/orders/{{order_id}}
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"time_in_force":"DAY","price":101,"quantity":8}
HTTP 200
[Asserts]
jsonpath "$.quantity" == 8
jsonpath "$.remaining_quantity" == 8
jsonpath "$.queue_sequence" == {{new_sequence}}

# Редактирование с нулевым количеством отклоняется: 422
PUT {{base_url}}/orders/{{order_id}}
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"time_in_force":"DAY","price":101,"quantity":0}
HTTP 422

# Получение CSRF перед регистрацией или входом
GET {{base_url}}/auth/csrf
HTTP 200
[Captures]
csrf: jsonpath "$.token"
[Asserts]
jsonpath "$.headerName" == "X-CSRF-TOKEN"

# Вход продавца
POST {{base_url}}/auth/login
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"username":"{{seller_username}}","password":"{{test_password}}"}
HTTP 200
[Asserts]
jsonpath "$.message" == "OK"

# Обновление CSRF после входа
GET {{base_url}}/auth/csrf
HTTP 200
[Captures]
csrf: jsonpath "$.token"
[Asserts]
jsonpath "$.headerName" == "X-CSRF-TOKEN"

# Проверка текущего пользователя
GET {{base_url}}/auth/me
HTTP 200
[Asserts]
jsonpath "$.username" == "{{seller_username}}"

# Другой пользователь не может отменить чужую заявку: 404
PATCH {{base_url}}/orders/{{order_id}}
X-CSRF-TOKEN: {{csrf}}
HTTP 404

# Другой пользователь не может изменить чужую заявку: 404
PUT {{base_url}}/orders/{{order_id}}
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"time_in_force":"DAY","price":102,"quantity":10}
HTTP 404

# Получение CSRF перед регистрацией или входом
GET {{base_url}}/auth/csrf
HTTP 200
[Captures]
csrf: jsonpath "$.token"
[Asserts]
jsonpath "$.headerName" == "X-CSRF-TOKEN"

# Вход покупателя
POST {{base_url}}/auth/login
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"username":"{{buyer_username}}","password":"{{test_password}}"}
HTTP 200
[Asserts]
jsonpath "$.message" == "OK"

# Обновление CSRF после входа
GET {{base_url}}/auth/csrf
HTTP 200
[Captures]
csrf: jsonpath "$.token"
[Asserts]
jsonpath "$.headerName" == "X-CSRF-TOKEN"

# Проверка текущего пользователя
GET {{base_url}}/auth/me
HTTP 200
[Asserts]
jsonpath "$.username" == "{{buyer_username}}"

# Владелец отменяет заявку: 204
PATCH {{base_url}}/orders/{{order_id}}
X-CSRF-TOKEN: {{csrf}}
HTTP 204

# После отмены статус CANCELLED, исполненное количество не изменилось
GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Asserts]
jsonpath "$.items[0].status" == "CANCELLED"
jsonpath "$.items[0].filled_quantity" == 0
jsonpath "$.items[0].remaining_quantity" == 8

# Отменённая заявка отсутствует в стакане
GET {{base_url}}/instruments/{{sec_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.bids" count == 0
jsonpath "$.asks" count == 0
jsonpath "$.best_bid" == null

# Повторная отмена неактивной заявки: 409
PATCH {{base_url}}/orders/{{order_id}}
X-CSRF-TOKEN: {{csrf}}
HTTP 409
[Asserts]
jsonpath "$.message" == "ORDER_NOT_ACTIVE"

# Изменение отменённой заявки: 409
PUT {{base_url}}/orders/{{order_id}}
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"time_in_force":"DAY","price":102,"quantity":10}
HTTP 409

# Отмена несуществующей заявки: 404
PATCH {{base_url}}/orders/9223372036854775807
X-CSRF-TOKEN: {{csrf}}
HTTP 404
