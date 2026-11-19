# Выставление заявок

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

# Корректная заявка: цена 100, количество 10, срок DAY
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":10}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Заявка принята в очередь: NEW, исполнено 0, остаток 10
GET {{base_url}}/orders?secCode={{sec_code}}
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1
jsonpath "$.items[0].status" == "NEW"
jsonpath "$.items[0].price" == 100
jsonpath "$.items[0].quantity" == 10
jsonpath "$.items[0].initial_quantity" == 10
jsonpath "$.items[0].filled_quantity" == 0
jsonpath "$.items[0].remaining_quantity" == 10
jsonpath "$.items[0].matching_pending" == false

# Нулевое количество отклоняется: 422
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":0}
HTTP 422

# Нулевая цена отклоняется: 422
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":0,"quantity":10}
HTTP 422

# Цена не соответствует шагу 0.01: 409
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100.001,"quantity":10}
HTTP 409

# Количество 15 не кратно размеру лота 10: 409
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":15}
HTTP 409

# Заявка на чужом торговом счёте недоступна: 404
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{seller_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":10}
HTTP 404

# Несуществующий инструмент: 404
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{missing_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":10}
HTTP 404

# Ошибочные запросы не создали дополнительных заявок
GET {{base_url}}/orders?secCode={{sec_code}}&direction=BUY
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1

# Выход из учётной записи
POST {{base_url}}/auth/logout
X-CSRF-TOKEN: {{csrf}}
HTTP 200
[Asserts]
jsonpath "$.message" == "OK"

# Реестр заявок без авторизации недоступен: 401
GET {{base_url}}/orders
HTTP 401
[Asserts]
jsonpath "$.message" == "AUTHENTICATION_REQUIRED"
