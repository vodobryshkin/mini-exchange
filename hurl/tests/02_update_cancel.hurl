# Редактирование и отмена

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
HTTP 200
[Captures]
order_id: jsonpath "$.items[0].id"
old_sequence: jsonpath "$.items[0].queue_sequence"
[Asserts]
jsonpath "$.total_elements" == 1

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

PUT {{base_url}}/orders/{{order_id}}
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"time_in_force":"DAY","price":101,"quantity":8}
HTTP 200
[Asserts]
jsonpath "$.quantity" == 8
jsonpath "$.remaining_quantity" == 8
jsonpath "$.queue_sequence" == {{new_sequence}}

PUT {{base_url}}/orders/{{order_id}}
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"time_in_force":"DAY","price":101,"quantity":0}
HTTP 422

GET {{base_url}}/auth/csrf
HTTP 200
[Captures]
csrf: jsonpath "$.token"
[Asserts]
jsonpath "$.headerName" == "X-CSRF-TOKEN"

POST {{base_url}}/auth/login
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"username":"{{seller_username}}","password":"{{test_password}}"}
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
jsonpath "$.username" == "{{seller_username}}"

PATCH {{base_url}}/orders/{{order_id}}
X-CSRF-TOKEN: {{csrf}}
HTTP 404

PUT {{base_url}}/orders/{{order_id}}
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"time_in_force":"DAY","price":102,"quantity":10}
HTTP 404

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

PATCH {{base_url}}/orders/{{order_id}}
X-CSRF-TOKEN: {{csrf}}
HTTP 204

GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Asserts]
jsonpath "$.items[0].status" == "CANCELLED"
jsonpath "$.items[0].filled_quantity" == 0
jsonpath "$.items[0].remaining_quantity" == 8

GET {{base_url}}/instruments/{{sec_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.bids" count == 0
jsonpath "$.asks" count == 0
jsonpath "$.best_bid" == null

PATCH {{base_url}}/orders/{{order_id}}
X-CSRF-TOKEN: {{csrf}}
HTTP 409
[Asserts]
jsonpath "$.message" == "ORDER_NOT_ACTIVE"

PUT {{base_url}}/orders/{{order_id}}
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"time_in_force":"DAY","price":102,"quantity":10}
HTTP 409

PATCH {{base_url}}/orders/9223372036854775807
X-CSRF-TOKEN: {{csrf}}
HTTP 404

