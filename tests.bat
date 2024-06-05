@echo off

echo ---- Place order
curl -X POST http://localhost/order -H "Content-Type: application/json" -d "{\"customerId\": \"margareta\",\"items\": [{\"productId\": 1,\"count\": 1}],\"shippingAddress\": \"shipping address\"}"

set /p orderId="---- Enter new orderId: "

echo ---- Get order
curl -X GET http://localhost/order/%orderId%

echo ---- Confirm payment from Gateway
curl -X PUT -H "Content-Type: application/json" --data-raw true  http://localhost/payment/%orderId%/paid

timeout /t 1

echo ---- Get order
curl -X GET http://localhost/order/%orderId%

echo ---- Confirm shipping
curl -X PUT -H "Content-Type: application/json" --data-raw true  http://localhost/shipping/%orderId%/status

timeout /t 1

echo ---- Get order
curl -X GET http://localhost/order/%orderId%

timeout /t 3

echo ---- Get order (bis)
curl -X GET http://localhost/order/%orderId%

echo DONE