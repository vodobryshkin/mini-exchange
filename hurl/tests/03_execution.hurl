# Исполнение заявок

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

# Подготовка PTP: первая продажа по 101
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":101,"quantity":2}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Сохранение идентификатора продажи по 101
GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
sell_101: jsonpath "$.items[0].id"

# Подготовка PTP: первая продажа по более выгодной цене 100
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":3}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Сохранение идентификатора первой продажи по 100
GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
sell_100_first: jsonpath "$.items[0].id"

# Подготовка PTP: вторая продажа по той же цене 100
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":2}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Сохранение идентификатора второй продажи по 100
GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
sell_100_second: jsonpath "$.items[0].id"

# Ожидание постановки продаж в стакан: два уровня, три заявки
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

# Входящая покупка по 105 на 6 единиц пересекает обе цены продаж
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":105,"quantity":6}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Сохранение идентификатора входящей покупки
GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
buy_id: jsonpath "$.items[0].id"

# Ожидание полного исполнения покупки: FILLED, остаток 0
GET {{base_url}}/orders?secCode={{sec_code}}
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.items[0].status" == "FILLED"
jsonpath "$.items[0].filled_quantity" == 6
jsonpath "$.items[0].remaining_quantity" == 0

# PTP: сначала цена 100, при равной цене — очередь; сделки по цене стоящей заявки
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

# Полностью исполненную заявку отменить нельзя: 409
PATCH {{base_url}}/orders/{{buy_id}}
X-CSRF-TOKEN: {{csrf}}
HTTP 409
[Asserts]
jsonpath "$.message" == "ORDER_NOT_ACTIVE"

# Частично исполненная продажа по 101 остаётся в стакане с остатком 1
GET {{base_url}}/instruments/{{sec_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.asks[0].price" == 101
jsonpath "$.asks[0].quantity" == 1
jsonpath "$.asks[0].order_count" == 1

# Обратный PTP: первая покупка по 105
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{reverse_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":105,"quantity":2}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Сохранение идентификатора первой покупки по 105
GET {{base_url}}/orders?secCode={{reverse_code}}
HTTP 200
[Captures]
buy_105_first: jsonpath "$.items[0].id"

# Обратный PTP: покупка по менее выгодной цене 104
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{reverse_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":104,"quantity":3}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Обратный PTP: вторая покупка по 105
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{reverse_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":105,"quantity":2}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Сохранение идентификатора второй покупки по 105
GET {{base_url}}/orders?secCode={{reverse_code}}
HTTP 200
[Captures]
buy_105_second: jsonpath "$.items[0].id"

# Ожидание готовности очереди покупок по 105
GET {{base_url}}/instruments/{{reverse_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.bids[0].price" == 105
jsonpath "$.bids[0].quantity" == 4
jsonpath "$.bids[0].order_count" == 2

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

# Входящая продажа по 100 на 3 единицы пересекает очередь покупок
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{reverse_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":3}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# PTP для покупок: лучшая цена 105 и порядок поступления; исполнение 2 + 1
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

# Покупка со счёта без денег принимается на обработку
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_second_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":105,"quantity":1}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Сохранение идентификатора покупки без обеспечения
GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
unfunded_buy: jsonpath "$.items[0].id"

# При исполнении покупка без денег отменяется, исполненное количество равно 0
GET {{base_url}}/orders?secCode={{sec_code}}
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.items[0].status" == "CANCELLED"
jsonpath "$.items[0].filled_quantity" == 0

# Подготовка самосделки: продажа с другого счёта того же пользователя
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{second_code}}"},"trading_account_id":{{buyer_second_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":1}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Ожидание размещения собственной продажи в стакане
GET {{base_url}}/instruments/{{second_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.asks[0].quantity" == 1

# Встречная покупка того же пользователя с другого счёта
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{second_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":1}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Входящая заявка для самосделки отменяется
GET {{base_url}}/orders?secCode={{second_code}}
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.items[0].status" == "CANCELLED"

# Самосделка не создала сделок
GET {{base_url}}/trades?secCode={{second_code}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 0

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

# Продажа со счёта без позиции принимается на обработку
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{empty_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":1}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Сохранение идентификатора необеспеченной продажи
GET {{base_url}}/orders?secCode={{empty_code}}
HTTP 200
[Captures]
unfunded_sell: jsonpath "$.items[0].id"

# Необеспеченная продажа находится в очереди до попытки исполнения
GET {{base_url}}/instruments/{{empty_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.asks[0].quantity" == 1

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

# Встречная покупка запускает проверку обеспечения продавца
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{empty_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":1}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Продажа без позиции удалена; покупка остаётся в стакане
GET {{base_url}}/instruments/{{empty_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.bids[0].quantity" == 1
jsonpath "$.asks" count == 0

# Недостаток позиции не привёл к сделке
GET {{base_url}}/trades?secCode={{empty_code}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 0

# Заранее просроченная DAY-заявка получает статус EXPIRED
GET {{base_url}}/orders?secCode={{day_code}}
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1
jsonpath "$.items[0].status" == "EXPIRED"

# Просроченная DAY-заявка не попадает в стакан
GET {{base_url}}/instruments/{{day_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.bids" count == 0
jsonpath "$.asks" count == 0

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

# В реестре продавца необеспеченная заявка имеет статус CANCELLED
GET {{base_url}}/orders?secCode={{empty_code}}
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.items[0].status" == "CANCELLED"
