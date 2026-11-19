# Реестры заявок и сделок

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
active_id: jsonpath "$.items[0].id"

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":99,"quantity":20}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
cancel_id: jsonpath "$.items[0].id"

PATCH {{base_url}}/orders/{{cancel_id}}
X-CSRF-TOKEN: {{csrf}}
HTTP 204

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{second_code}}"},"trading_account_id":{{buyer_second_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":50,"quantity":3}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/orders?secCode={{second_code}}
HTTP 200
[Captures]
second_account_order: jsonpath "$.items[0].id"

GET {{base_url}}/instruments/{{sec_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.bids[0].quantity" == 10

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

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":4}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
seller_id: jsonpath "$.items[0].id"

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{second_code}}"},"trading_account_id":{{seller_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":40,"quantity":7}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/orders?secCode={{second_code}}
HTTP 200
[Captures]
private_id: jsonpath "$.items[0].id"

GET {{base_url}}/trades?secCode={{sec_code}}
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1
jsonpath "$.items[0].buy_order_id" == {{active_id}}
jsonpath "$.items[0].sell_order_id" == {{seller_id}}
jsonpath "$.items[0].price" == 100
jsonpath "$.items[0].quantity" == 4

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

GET {{base_url}}/orders
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 3
jsonpath "$.items" count == 3
jsonpath "$.items[0].id" == {{second_account_order}}
jsonpath "$.items[1].id" == {{cancel_id}}
jsonpath "$.items[2].id" == {{active_id}}
jsonpath "$.items[2].filled_quantity" == 4
jsonpath "$.items[2].remaining_quantity" == 6

GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 2

GET {{base_url}}/orders?tradingAccountId={{buyer_second_account}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1
jsonpath "$.items[0].id" == {{second_account_order}}

GET {{base_url}}/orders?tradingAccountId={{seller_account}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 0
jsonpath "$.items" count == 0

GET {{base_url}}/orders?status=CANCELLED
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1
jsonpath "$.items[0].id" == {{cancel_id}}

GET {{base_url}}/orders?direction=SELL
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 0

GET {{base_url}}/orders?page=0&size=2
HTTP 200
[Asserts]
jsonpath "$.page" == 0
jsonpath "$.size" == 2
jsonpath "$.total_elements" == 3
jsonpath "$.items" count == 2

GET {{base_url}}/orders?page=1&size=2
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 3
jsonpath "$.items" count == 1
jsonpath "$.items[0].id" == {{active_id}}

GET {{base_url}}/trades?secCode={{sec_code}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1
jsonpath "$.items[0].buy_trading_account_id" == {{buyer_account}}
jsonpath "$.items[0].sell_trading_account_id" == {{seller_account}}

GET {{base_url}}/trades?secCode={{sec_code}}&tradingAccountId={{buyer_second_account}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 0

GET {{base_url}}/trades?secCode={{sec_code}}&tradingAccountId={{seller_account}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 0

GET {{base_url}}/trades?secCode={{sec_code}}&page=1&size=1
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1
jsonpath "$.items" count == 0

GET {{base_url}}/trades?from=2000-01-01T00:00:00Z&to=2100-01-01T00:00:00Z
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1

GET {{base_url}}/orders?from=2100-01-01T00:00:00Z
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 0

GET {{base_url}}/orders?page=-1
HTTP 400

GET {{base_url}}/orders?size=0
HTTP 400

GET {{base_url}}/orders?from=2100-01-01T00:00:00Z&to=2000-01-01T00:00:00Z
HTTP 400

GET {{base_url}}/trades?size=101
HTTP 400

