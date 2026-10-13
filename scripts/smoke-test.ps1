# Run after starting Spring Boot. PowerShell 5+.
$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/api'
Write-Host '1. Checking public health...' -ForegroundColor Cyan
Invoke-RestMethod "$base/health" | Format-List

Write-Host '2. Login admin...' -ForegroundColor Cyan
$login = @{ username = 'admin'; password = 'Admin@12345' } | ConvertTo-Json
$auth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -Body $login -ContentType 'application/json'
$headers = @{ Authorization = "Bearer $($auth.token)" }

Write-Host '3. Products and dashboard...' -ForegroundColor Cyan
$products = @(Invoke-RestMethod -Uri "$base/products" -Headers $headers)
$products | Format-Table id, sku, nameCn, stockQuantity
Invoke-RestMethod -Uri "$base/dashboard" -Headers $headers | Format-List

Write-Host '4. Create order (SKU CN-001 quantity 2)...' -ForegroundColor Cyan
$product = $products | Where-Object { $_.sku -eq 'CN-001' } | Select-Object -First 1
if (-not $product) { throw 'Seed product CN-001 missing. Check SEED_DEMO=true' }
$payload = @{ items = @( @{ productId = $product.id; quantity = 2 } ) } | ConvertTo-Json -Depth 5
$order = Invoke-RestMethod -Uri "$base/orders" -Method Post -Headers $headers -ContentType 'application/json' -Body $payload
$order | Format-List id, orderNumber, status, totalAmountCny

Write-Host '5. Confirm order (decrease stock by 2)...' -ForegroundColor Cyan
Invoke-RestMethod -Uri "$base/orders/$($order.id)/confirm" -Method Patch -Headers $headers | Format-List id, status
Invoke-RestMethod -Uri "$base/products/$($product.id)" -Headers $headers | Format-List sku, stockQuantity

Write-Host '6. Cancel order (restore stock by 2)...' -ForegroundColor Cyan
Invoke-RestMethod -Uri "$base/orders/$($order.id)/cancel" -Method Patch -Headers $headers | Format-List id, status
Invoke-RestMethod -Uri "$base/products/$($product.id)" -Headers $headers | Format-List sku, stockQuantity

Write-Host '7. Inventory logs...' -ForegroundColor Cyan
Invoke-RestMethod -Uri "$base/inventory/logs" -Headers $headers | Select-Object -First 5 | Format-Table sku, changeQuantity, resultingStock, reason
Write-Host 'SMOKE TEST FINISHED' -ForegroundColor Green
