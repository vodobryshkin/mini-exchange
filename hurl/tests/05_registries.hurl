# Реестры заявок и сделок

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

# Создание активной покупки для реестра
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":10}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Сохранение идентификатора активной заявки
GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
active_id: jsonpath "$.items[0].id"

# Создание второй покупки для отмены
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{buyer_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":99,"quantity":20}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Сохранение идентификатора отменяемой заявки
GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
cancel_id: jsonpath "$.items[0].id"

# Отмена второй покупки для фильтра по статусу
PATCH {{base_url}}/orders/{{cancel_id}}
X-CSRF-TOKEN: {{csrf}}
HTTP 204

# Заявка на другом инструменте и другом счёте покупателя
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{second_code}}"},"trading_account_id":{{buyer_second_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":50,"quantity":3}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Сохранение идентификатора заявки второго счёта
GET {{base_url}}/orders?secCode={{second_code}}
HTTP 200
[Captures]
second_account_order: jsonpath "$.items[0].id"

# Ожидание размещения активной покупки перед исполнением
GET {{base_url}}/instruments/{{sec_code}}/orderbook?depth=20
[Options]
retry: 50
retry-interval: 100ms
HTTP 200
[Asserts]
jsonpath "$.bids[0].quantity" == 10

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

# Продажа 4 единиц для частичного исполнения покупки
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{sec_code}}"},"trading_account_id":{{seller_account}},"direction":"SELL","type":"LIMIT","time_in_force":"DAY","price":100,"quantity":4}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Сохранение идентификатора продажи
GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Captures]
seller_id: jsonpath "$.items[0].id"

# Отдельная заявка продавца для проверки изоляции реестров
POST {{base_url}}/orders
X-CSRF-TOKEN: {{csrf}}
Content-Type: application/json
{"sec_code":{"value":"{{second_code}}"},"trading_account_id":{{seller_account}},"direction":"BUY","type":"LIMIT","time_in_force":"DAY","price":40,"quantity":7}
HTTP 201
[Asserts]
jsonpath "$.message" == "CREATED"

# Сохранение идентификатора отдельной заявки продавца
GET {{base_url}}/orders?secCode={{second_code}}
HTTP 200
[Captures]
private_id: jsonpath "$.items[0].id"

# Ожидание сделки: правильные заявки, цена 100, количество 4
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

# Реестр содержит только три заявки покупателя, новые первыми; остаток частичной заявки 6
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

# Фильтр заявок по инструменту возвращает две заявки
GET {{base_url}}/orders?secCode={{sec_code}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 2

# Фильтр по своему второму счёту возвращает одну заявку
GET {{base_url}}/orders?tradingAccountId={{buyer_second_account}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1
jsonpath "$.items[0].id" == {{second_account_order}}

# Фильтр по чужому счёту не раскрывает чужие заявки
GET {{base_url}}/orders?tradingAccountId={{seller_account}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 0
jsonpath "$.items" count == 0

# Фильтр CANCELLED возвращает только отменённую заявку
GET {{base_url}}/orders?status=CANCELLED
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1
jsonpath "$.items[0].id" == {{cancel_id}}

# У покупателя нет заявок SELL
GET {{base_url}}/orders?direction=SELL
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 0

# Первая страница заявок: две записи из трёх
GET {{base_url}}/orders?page=0&size=2
HTTP 200
[Asserts]
jsonpath "$.page" == 0
jsonpath "$.size" == 2
jsonpath "$.total_elements" == 3
jsonpath "$.items" count == 2

# Вторая страница заявок: оставшаяся запись
GET {{base_url}}/orders?page=1&size=2
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 3
jsonpath "$.items" count == 1
jsonpath "$.items[0].id" == {{active_id}}

# Реестр сделок покупателя: правильные счета обеих сторон
GET {{base_url}}/trades?secCode={{sec_code}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1
jsonpath "$.items[0].buy_trading_account_id" == {{buyer_account}}
jsonpath "$.items[0].sell_trading_account_id" == {{seller_account}}

# На втором счёте покупателя сделок нет
GET {{base_url}}/trades?secCode={{sec_code}}&tradingAccountId={{buyer_second_account}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 0

# Фильтр сделок по чужому счёту возвращает пустой результат
GET {{base_url}}/trades?secCode={{sec_code}}&tradingAccountId={{seller_account}}
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 0

# Страница за пределами реестра сделок пуста, общее количество сохраняется
GET {{base_url}}/trades?secCode={{sec_code}}&page=1&size=1
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1
jsonpath "$.items" count == 0

# Фильтр сделок по широкому диапазону дат включает сделку
GET {{base_url}}/trades?from=2000-01-01T00:00:00Z&to=2100-01-01T00:00:00Z
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 1

# Фильтр заявок по будущей дате возвращает пустой результат
GET {{base_url}}/orders?from=2100-01-01T00:00:00Z
HTTP 200
[Asserts]
jsonpath "$.total_elements" == 0

# Отрицательный номер страницы отклоняется: 400
GET {{base_url}}/orders?page=-1
HTTP 400

# Нулевой размер страницы отклоняется: 400
GET {{base_url}}/orders?size=0
HTTP 400

# Начало диапазона позже конца отклоняется: 400
GET {{base_url}}/orders?from=2100-01-01T00:00:00Z&to=2000-01-01T00:00:00Z
HTTP 400

# Размер страницы сделок больше 100 отклоняется: 400
GET {{base_url}}/trades?size=101
HTTP 400
