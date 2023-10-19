# Place order
echo ---- Place order
curl -X POST http://localhost/order -H 'Content-Type: application/json' -d '{"customerId": "margareta","items": [{"productId": 1,"count": 1}],"shippingAddress": "shipping address"}'

sleep 1 # mq
echo "\n---- Confirm payment"
curl -X PUT -H 'Content-Type: application/json' --data-raw true  http://localhost/payment/2/status

sleep 1 # mq
echo "\n---- Get order"
curl -X GET http://localhost/order/2

echo "\n---- Confirm shipping"
curl -X PUT -H 'Content-Type: application/json' --data-raw true  http://localhost/shipping/2/status

sleep 1 # mq
echo "\n---- Get order"
curl -X GET http://localhost/order/2

echo "\nDONE"