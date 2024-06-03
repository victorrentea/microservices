# Place order
echo ---- Place order
curl -X POST http://localhost/order -H 'Content-Type: application/json' -d '{"customerId": "margareta","items": [{"productId": 1,"count": 1}],"shippingAddress": "shipping address"}'

printf "\n---- Enter new orderId:"
read orderId

echo "\n---- Get order"
curl -X GET http://localhost/order/$orderId

sleep 1 # mq
echo "\n---- Confirm payment from Gateway"
curl -X PUT -H 'Content-Type: application/json' --data-raw true  http://localhost/order/$orderId/paid
#curl -X PUT -H 'Content-Type: application/json' --data-raw true  http://localhost/payment/$orderId/paid

sleep 1 # mq
#sleep 6 # if increasing the delay, I will get to see the change in the order status (moving over queues)
echo "\n---- Get order"
curl -X GET http://localhost/order/$orderId

echo "\n---- Confirm shipping"
curl -X PUT -H 'Content-Type: application/json' --data-raw true  http://localhost/shipping/$orderId/status

sleep 1 # mq
echo "\n---- Get order"
curl -X GET http://localhost/order/$orderId

sleep 3 # mq
echo "\n---- Get order (bis)"
curl -X GET http://localhost/order/$orderId

echo "\nDONE"