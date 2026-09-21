# Run against the disposable upgrade-test database, after starting the JAR on 18080
# and `BACKEND_ORIGIN=http://127.0.0.1:18080 next start -p 13000`.
$ErrorActionPreference = 'Stop'
$backend = 'http://127.0.0.1:18080'
$frontend = 'http://127.0.0.1:13000'

function Check-Request($Label, $Url, $Status = 200, $Headers = @{}, $Body = $null, $Code = -1) {
    $parameters = @{ Uri = $Url; Headers = $Headers; SkipHttpErrorCheck = $true; TimeoutSec = 30 }
    if ($null -ne $Body) {
        $parameters.Method = 'POST'
        $parameters.ContentType = 'application/json'
        $parameters.Body = ($Body | ConvertTo-Json -Depth 8)
    }
    $raw = Invoke-WebRequest @parameters
    $content = if ($raw.Content -is [byte[]]) { [Text.Encoding]::UTF8.GetString($raw.Content) } else { $raw.Content }
    $response = [pscustomobject]@{ StatusCode = $raw.StatusCode; Headers = $raw.Headers; Content = $content }
    if ([int]$response.StatusCode -ne $Status) { throw "$Label HTTP expected $Status, got $($response.StatusCode)" }
    if ($response.Headers['Content-Type'] -match 'application/json' -and $Url -notmatch '/actuator/|/v3/') {
        $json = $response.Content | ConvertFrom-Json
        $expectedCode = if ($Code -ge 0) { $Code } elseif ($Status -eq 200) { 0 } else { $Status }
        if ($json.code -ne $expectedCode) { throw "$Label envelope code mismatch" }
    }
    Write-Host "PASS $Label"
    return $response
}

$health = Check-Request 'health' "$backend/actuator/health"
if (($health.Content | ConvertFrom-Json).status -ne 'UP') { throw 'Backend health is not UP' }
$null = Check-Request 'ping' "$backend/api/ping"
$schema = Check-Request 'OpenAPI' "$backend/v3/api-docs"
if (-not (($schema.Content | ConvertFrom-Json).paths.'/api/merchant/dashboard/metrics')) { throw 'OpenAPI missing business paths' }
$null = Check-Request 'Swagger UI' "$backend/swagger-ui/index.html"
$null = Check-Request 'anonymous rejected' "$backend/api/merchant/dashboard/metrics?startDate=2026-09-01&endDate=2026-09-05" 401
$null = Check-Request 'invalid login rejected' "$backend/api/auth/login" 401 @{} @{username='merchant_a_admin';password='wrong'}
# Existing GlobalExceptionHandler returns HTTP 200 with envelope 400 for @Valid errors.
# Assert that existing contract explicitly; fixing its HTTP semantics is a separate change.
$null = Check-Request 'validation rejected (legacy HTTP 200 / code 400)' "$backend/api/auth/login" 200 @{} @{username='';password=''} 400

$sessions = @{}
foreach ($username in @('merchant_a_admin', 'merchant_b_admin', 'consumer_001')) {
    $response = Check-Request "login $username" "$backend/api/auth/login" 200 @{} @{username=$username;password='123456'}
    $data = ($response.Content | ConvertFrom-Json).data
    $sessions[$username] = @{ Authorization = "Bearer $($data.accessToken)" }
    $null = Check-Request "identity $username" "$backend/api/auth/me" 200 $sessions[$username]
}
$null = Check-Request 'consumer merchant access denied' "$backend/api/merchant/dashboard/metrics?startDate=2026-09-01&endDate=2026-09-05" 403 $sessions.consumer_001
foreach ($username in @('merchant_a_admin','merchant_b_admin')) {
    $null = Check-Request "metrics $username" "$backend/api/merchant/dashboard/metrics?startDate=2026-09-01&endDate=2026-09-05" 200 $sessions[$username]
    $trend = Check-Request "trends $username" "$backend/api/merchant/dashboard/trends?startDate=2026-09-01&endDate=2026-09-05" 200 $sessions[$username]
    if ((($trend.Content | ConvertFrom-Json).data).Count -ne 5) { throw 'Trend date contract changed' }
}
$null = Check-Request 'trend date limit' "$backend/api/merchant/dashboard/trends?startDate=2026-01-01&endDate=2026-09-05" 400 $sessions.merchant_a_admin
$null = Check-Request 'public products' "$backend/api/public/stores/1001/products?page=1&size=10"
$first = Check-Request 'product cache miss/read' "$backend/api/public/stores/1001/products/1784970220000"
$second = Check-Request 'product cache hit/read' "$backend/api/public/stores/1001/products/1784970220000"
if ($first.Content -ne $second.Content) { throw 'Product cache changes the JSON response' }
$null = Check-Request 'cart' "$backend/api/cart/items" 200 $sessions.consumer_001
$null = Check-Request 'orders' "$backend/api/orders" 200 $sessions.consumer_001
$null = Check-Request 'after sales' "$backend/api/after-sales" 200 $sessions.consumer_001
$null = Check-Request 'public promotions' "$backend/api/public/promotions"

$null = Check-Request 'BFF public stores' "$frontend/api/backend/public/stores"
$login = Check-Request 'BFF login' "$frontend/api/session/login" 200 @{} @{username='merchant_a_admin';password='123456'}
$cookie = ($login.Headers['Set-Cookie'] | Select-Object -First 1).Split(';')[0]
if (-not $cookie) { throw 'BFF did not set session cookie' }
# Explicit Cookie header lets the local HTTP check exercise the production Secure cookie.
$null = Check-Request 'BFF authenticated metrics' "$frontend/api/backend/merchant/dashboard/metrics?startDate=2026-09-01&endDate=2026-09-05" 200 @{Cookie=$cookie}
$null = Check-Request 'BFF anonymous rejected' "$frontend/api/backend/merchant/dashboard/metrics?startDate=2026-09-01&endDate=2026-09-05" 401
$null = Check-Request 'store page' "$frontend/stores/1001"
Write-Host 'All HTTP/BFF checks passed; no token values were printed.'
