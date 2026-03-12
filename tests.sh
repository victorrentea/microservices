#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost}"

call_json() {
  local method="$1"
  local path="$2"
  local data="${3:-}"
  local tmp_body
  tmp_body=$(mktemp)

  local code
  if [[ -n "$data" ]]; then
    code=$(curl -s -o "$tmp_body" -w '%{http_code}' -X "$method" "$BASE_URL$path" -H 'Content-Type: application/json' --data-raw "$data")
  else
    code=$(curl -s -o "$tmp_body" -w '%{http_code}' -X "$method" "$BASE_URL$path")
  fi

  echo "[$method $path] -> HTTP $code"
  cat "$tmp_body"
  echo

  if [[ "$code" -lt 200 || "$code" -ge 300 ]]; then
    echo "Request failed: $method $path"
    rm -f "$tmp_body"
    exit 1
  fi

  rm -f "$tmp_body"
}

echo "---- Place order"
ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/order" -H 'Content-Type: application/json' -d '{"customerId":"margareta","items":[{"productId":1,"count":1}],"shippingAddress":"shipping address"}')
echo "$ORDER_RESPONSE"

# Response format is payment URL containing ...&orderId=<id>
orderId=$(echo "$ORDER_RESPONSE" | sed -n 's/.*orderId=\([0-9][0-9]*\).*/\1/p')
if [[ -z "${orderId:-}" ]]; then
  echo "Could not extract orderId from response."
  echo "Fallback to manual input."
  read -r -p "Enter orderId: " orderId
fi

echo "\n---- Confirm payment from Gateway"
call_json PUT "/payment/$orderId/status" "true"

sleep 1

echo "\n---- Get order"
call_json GET "/order/$orderId"

echo "\n---- Confirm shipping"
call_json PUT "/shipping/$orderId/status" "true"

sleep 1

echo "\n---- Get order"
call_json GET "/order/$orderId"

sleep 3

echo "\n---- Get order (bis)"
call_json GET "/order/$orderId"

echo "\nDONE"