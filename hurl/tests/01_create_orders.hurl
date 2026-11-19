# Выставление заявок

GET {{base_url}}/auth/csrf
HTTP 200
[Captures]
csrf: jsonpath "$.token"
[Asserts]
jsonpath "$.headerName" == "X-CSRF-TOKEN"

POST {{base_url}}/auth/register
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"username":"{{buyer_username}}","password":"{{test_password}}"}
HTTP 201
[Asserts]
jsonpath "$.message" == "OK"

POST {{base_url}}/auth/register
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"username":"{{seller_username}}","password":"{{test_password}}"}
HTTP 201
[Asserts]
jsonpath "$.message" == "OK"


GET {{base_url}}/auth/csrf
HTTP 200
[Captures]
csrf: jsonpath "$.token"
[Asserts]
jsonpath "$.headerName" == "X-CSRF-TOKEN"

POST {{base_url}}/auth/login
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"username":"{{buyer_username}}","password":"{{test_password}}"}
HTTP 200
[Asserts]
jsonpath "$.message" == "OK"

GET {{base_url}}/auth/csrf
HTTP 200
[Captures]
csrf: jsonpath "$.token"
[Asserts]
jsonpath "$.headerName" == "X-CSRF-TOKEN"

GET {{base_url}}/auth/me
HTTP 200
[Asserts]
jsonpath "$.username" == "{{buyer_username}}"

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":10}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

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

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":0}
HTTP 422

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":0,"quantity":10}
HTTP 422

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100.001,"quantity":10}
HTTP 409

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":15}
HTTP 409

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{seller_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":10}
HTTP 404

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{missing_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":10}
HTTP 404

GET {{base_url}}/orders?secCode={{sec_code}}&direction=BUY
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1

POST {{base_url}}/auth/logout
X-CSRF-TOKEN: {{csrf}}
HTTP 200
[Asserts]
jsonpath "$.message" == "OK"

GET {{base_url}}/orders
HTTP 401
[Asserts]
jsonpath "$.message" == "AUTHENTICATION_REQUIRED"

