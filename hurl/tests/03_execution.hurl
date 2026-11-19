# Исполнение заявок

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
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":101,"quantity":2}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
sell_101: jsonpath "$.items[0].id"

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":3}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
sell_100_first: jsonpath "$.items[0].id"

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":2}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
sell_100_second: jsonpath "$.items[0].id"

GET {{base_url}}/instruments/{{sec_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.asks[0].price" == 100
jsonpath "$.asks[0].quantity" == 5
jsonpath "$.asks[0].order_count" == 2
jsonpath "$.asks[1].quantity" == 2

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
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":105,"quantity":6}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
buy_id: jsonpath "$.items[0].id"

GET {{base_url}}/orders?secCode={{sec_code}}
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.items[0].status" == "FILLED"
jsonpath "$.items[0].filled_quantity" == 6
jsonpath "$.items[0].remaining_quantity" == 0

GET {{base_url}}/trades?secCode={{sec_code}}
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 3
jsonpath "$.items[0].sell_order_id" == {{sell_101}}
jsonpath "$.items[0].price" == 101
jsonpath "$.items[0].quantity" == 1
jsonpath "$.items[1].sell_order_id" == {{sell_100_second}}
jsonpath "$.items[1].price" == 100
jsonpath "$.items[1].quantity" == 2
jsonpath "$.items[2].sell_order_id" == {{sell_100_first}}
jsonpath "$.items[2].price" == 100
jsonpath "$.items[2].quantity" == 3

PATCH {{base_url}}/orders/{{buy_id}}
X-CSRF-TOKEN: {{csrf}}
HTTP 409
[Asserts]
jsonpath "$.message" == "ORDER_NOT_ACTIVE"

GET {{base_url}}/instruments/{{sec_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.asks[0].price" == 101
jsonpath "$.asks[0].quantity" == 1
jsonpath "$.asks[0].order_count" == 1

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{reverse_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":105,"quantity":2}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/orders?secCode={{reverse_code}}
HTTP 200
[Captures]
buy_105_first: jsonpath "$.items[0].id"

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{reverse_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":104,"quantity":3}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{reverse_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":105,"quantity":2}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/orders?secCode={{reverse_code}}
HTTP 200
[Captures]
buy_105_second: jsonpath "$.items[0].id"

GET {{base_url}}/instruments/{{reverse_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.bids[0].price" == 105
jsonpath "$.bids[0].quantity" == 4
jsonpath "$.bids[0].order_count" == 2

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
{"sec_code":{"value":"{{reverse_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":3}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/trades?secCode={{reverse_code}}
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 2
jsonpath "$.items[0].buy_order_id" == {{buy_105_second}}
jsonpath "$.items[0].price" == 105
jsonpath "$.items[0].quantity" == 1
jsonpath "$.items[1].buy_order_id" == {{buy_105_first}}
jsonpath "$.items[1].price" == 105
jsonpath "$.items[1].quantity" == 2

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
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_second_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":105,"quantity":1}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
unfunded_buy: jsonpath "$.items[0].id"

GET {{base_url}}/orders?secCode={{sec_code}}
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.items[0].status" == "CANCELLED"
jsonpath "$.items[0].filled_quantity" == 0

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{second_code}}"},"trading_account_id":{{buyer_second_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":1}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/instruments/{{second_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.asks[0].quantity" == 1

POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{second_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":1}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/orders?secCode={{second_code}}
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.items[0].status" == "CANCELLED"

GET {{base_url}}/trades?secCode={{second_code}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 0

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
{"sec_code":{"value":"{{empty_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":1}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/orders?secCode={{empty_code}}
HTTP 200
[Captures]
unfunded_sell: jsonpath "$.items[0].id"

GET {{base_url}}/instruments/{{empty_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.asks[0].quantity" == 1

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
{"sec_code":{"value":"{{empty_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":1}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

GET {{base_url}}/instruments/{{empty_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.bids[0].quantity" == 1
jsonpath "$.asks" count == 0

GET {{base_url}}/trades?secCode={{empty_code}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 0

GET {{base_url}}/orders?secCode={{day_code}}
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1
jsonpath "$.items[0].status" == "EXPIRED"

GET {{base_url}}/instruments/{{day_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.bids" count == 0
jsonpath "$.asks" count == 0

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

GET {{base_url}}/orders?secCode={{empty_code}}
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.items[0].status" == "CANCELLED"

