# Place order
echo ---- Place order
curl -X POST http://localhost/order -H 'Content-Type: application/json' -d '{"customerId": "margareta","items": [{"productId": 1,"count": 1}],"shippingAddress": "shipping address"}'

printf "\n---- Enter new orderId:"
read orderId

#sleep 1 # mq
echo "\n---- Confirm payment"
curl -X PUT -H 'Content-Type: application/json' --data-raw true  http://localhost/payment/$orderId/status

sleep 1 # mq
echo "\n---- Get order"
curl -X GET http://localhost/order/$orderId

echo "\n---- Confirm shipping"
curl -X PUT -H 'Content-Type: application/json' --data-raw true  http://localhost/shipping/$orderId/status

sleep 1 # mq
echo "\n---- Get order"
curl -X GET http://localhost/order/$orderId

echo "\nDONE"