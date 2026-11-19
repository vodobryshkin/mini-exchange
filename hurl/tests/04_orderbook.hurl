# Биржевой стакан

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

# Первая покупка по 100: количество 10
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":10}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Вторая покупка по 100: количество 9
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":9}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Покупка по 99: второй ценовой уровень
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":99,"quantity":20}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Покупка по 98: третий ценовой уровень
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":98,"quantity":30}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Агрегация равной цены: количество 19, количество заявок 2
GET {{base_url}}/instruments/{{sec_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.bids[0].quantity" == 19
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

# Частичное исполнение первой покупки: продажа 4 по 100
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":4}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Ожидание сделки на 4 единицы
GET {{base_url}}/trades?secCode={{sec_code}}
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1
jsonpath "$.items[0].quantity" == 4

# Продажа по 101: первый уровень asks
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":101,"quantity":8}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Первая продажа по 102: количество 5
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":102,"quantity":5}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Вторая продажа по 102: количество 7
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":102,"quantity":7}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Продажа по 103: третий уровень asks
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":103,"quantity":7}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# depth=2: остатки 15 и 20, сортировка сторон, best bid/ask, spread и глубина 35/20
GET {{base_url}}/instruments/{{sec_code}}/orderbook?depth=2
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.sec_code" == "{{sec_code}}"
jsonpath "$.depth" == 2
jsonpath "$.bids" count == 2
jsonpath "$.asks" count == 2
jsonpath "$.bids[0].price" == 100
jsonpath "$.bids[0].quantity" == 15
jsonpath "$.bids[0].order_count" == 2
jsonpath "$.bids[1].price" == 99
jsonpath "$.asks[0].price" == 101
jsonpath "$.asks[0].quantity" == 8
jsonpath "$.asks[1].price" == 102
jsonpath "$.asks[1].quantity" == 12
jsonpath "$.asks[1].order_count" == 2
jsonpath "$.best_bid" == 100
jsonpath "$.best_ask" == 101
jsonpath "$.spread" == 1
jsonpath "$.market_depth.bid_quantity" == 35
jsonpath "$.market_depth.ask_quantity" == 20

# depth=1 ограничивает каждую сторону и меняет глубину на 15/8
GET {{base_url}}/instruments/{{sec_code}}/orderbook?depth=1
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.bids" count == 1
jsonpath "$.asks" count == 1
jsonpath "$.market_depth.bid_quantity" == 15
jsonpath "$.market_depth.ask_quantity" == 8

# Без depth используется 20; возвращаются все доступные три уровня
GET {{base_url}}/instruments/{{sec_code}}/orderbook
HTTP 200
[Asserts]
jsonpath "$.depth" == 20
jsonpath "$.bids" count == 3
jsonpath "$.asks" count == 3

# Путь /api и максимальная допустимая глубина 100
GET {{base_url}}/instruments/{{sec_code}}/orderbook?depth=100
HTTP 200
[Asserts]
jsonpath "$.depth" == 100
jsonpath "$.bids" count == 3

# Глубина меньше 1 отклоняется: 400
GET {{base_url}}/instruments/{{sec_code}}/orderbook?depth=0
HTTP 400

# Глубина больше 100 отклоняется: 400
GET {{base_url}}/instruments/{{sec_code}}/orderbook?depth=101
HTTP 400

# Нечисловая глубина отклоняется: 400
GET {{base_url}}/instruments/{{sec_code}}/orderbook?depth=abc
HTTP 400

# Несуществующий инструмент: 404
GET {{base_url}}/instruments/{{missing_code}}/orderbook
HTTP 404

# Пустой стакан: массивы пусты, nullable-поля присутствуют, глубина равна 0
GET {{base_url}}/instruments/{{empty_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.bids" count == 0
jsonpath "$.asks" count == 0
jsonpath "$.best_bid" == null
jsonpath "$.best_ask" == null
jsonpath "$.spread" == null
jsonpath "$.market_depth.bid_quantity" == 0
jsonpath "$.market_depth.ask_quantity" == 0

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

# Подготовка одностороннего стакана: только покупка по 50
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{second_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":50,"quantity":1}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Без продаж best_ask и spread равны null, ask_quantity равен 0
GET {{base_url}}/instruments/{{second_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.best_bid" == 50
jsonpath "$.best_ask" == null
jsonpath "$.spread" == null
jsonpath "$.market_depth.ask_quantity" == 0
