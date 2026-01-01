# Auth Service Testing Script

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "WHO Cloud Auth Service - API Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080/api/auth"
$username = "testuser_$(Get-Random)"
$email = "test_$(Get-Random)@example.com"
$password = "TestPassword123!"

# Test 1: Register User
Write-Host "Test 1: Register New User" -ForegroundColor Yellow
Write-Host "Username: $username" -ForegroundColor Gray
Write-Host "Email: $email" -ForegroundColor Gray

try {
    $registerBody = @{
        username = $username
        email = $email
        password = $password
        fullName = "Test User"
    } | ConvertTo-Json

    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/register" `
        -Method Post `
        -ContentType "application/json" `
        -Body $registerBody `
        -ErrorAction Stop

    if ($registerResponse.success) {
        Write-Host "✓ Registration successful" -ForegroundColor Green
        $accessToken = $registerResponse.data.accessToken
        $refreshToken = $registerResponse.data.refreshToken
        Write-Host "  Access Token (first 50 chars): $($accessToken.Substring(0, [Math]::Min(50, $accessToken.Length)))..." -ForegroundColor Gray
        Write-Host "  Refresh Token: $($refreshToken.Substring(0, [Math]::Min(36, $refreshToken.Length)))" -ForegroundColor Gray
        Write-Host "  User ID: $($registerResponse.data.user.id)" -ForegroundColor Gray
        Write-Host "  Role: $($registerResponse.data.user.role)" -ForegroundColor Gray
    } else {
        Write-Host "✗ Registration failed" -ForegroundColor Red
        Write-Host $registerResponse
        exit 1
    }
} catch {
    Write-Host "✗ Registration error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 2: Login
Write-Host "Test 2: Login with Credentials" -ForegroundColor Yellow

try {
    $loginBody = @{
        username = $username
        password = $password
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop

    if ($loginResponse.success) {
        Write-Host "✓ Login successful" -ForegroundColor Green
        $accessToken = $loginResponse.data.accessToken
        $refreshToken = $loginResponse.data.refreshToken
        Write-Host "  New Access Token received" -ForegroundColor Gray
    } else {
        Write-Host "✗ Login failed" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Login error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 3: Get Current User
Write-Host "Test 3: Get Current User (Protected Endpoint)" -ForegroundColor Yellow

try {
    $headers = @{
        Authorization = "Bearer $accessToken"
    }

    $meResponse = Invoke-RestMethod -Uri "$baseUrl/me" `
        -Method Get `
        -Headers $headers `
        -ErrorAction Stop

    if ($meResponse.success) {
        Write-Host "✓ Retrieved current user" -ForegroundColor Green
        Write-Host "  Username: $($meResponse.data.username)" -ForegroundColor Gray
        Write-Host "  Authorities: $($meResponse.data.authorities[0].authority)" -ForegroundColor Gray
        Write-Host "  Account Status: Enabled=$($meResponse.data.enabled), NonExpired=$($meResponse.data.accountNonExpired)" -ForegroundColor Gray
    } else {
        Write-Host "✗ Failed to get current user" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Get current user error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 4: Validate Token
Write-Host "Test 4: Validate Token" -ForegroundColor Yellow

try {
    $validateBody = @{
        token = $accessToken
    } | ConvertTo-Json

    $validateResponse = Invoke-RestMethod -Uri "$baseUrl/validate" `
        -Method Post `
        -ContentType "application/json" `
        -Body $validateBody `
        -ErrorAction Stop

    if ($validateResponse.success -and $validateResponse.data.valid) {
        Write-Host "✓ Token is valid" -ForegroundColor Green
        Write-Host "  Username: $($validateResponse.data.username)" -ForegroundColor Gray
        Write-Host "  Email: $($validateResponse.data.email)" -ForegroundColor Gray
        Write-Host "  Role: $($validateResponse.data.role)" -ForegroundColor Gray
        Write-Host "  Message: $($validateResponse.data.message)" -ForegroundColor Gray
    } else {
        Write-Host "✗ Token validation failed" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Validate token error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 5: Refresh Token
Write-Host "Test 5: Refresh Access Token" -ForegroundColor Yellow

try {
    $refreshBody = @{
        refreshToken = $refreshToken
    } | ConvertTo-Json

    $refreshResponse = Invoke-RestMethod -Uri "$baseUrl/refresh" `
        -Method Post `
        -ContentType "application/json" `
        -Body $refreshBody `
        -ErrorAction Stop

    if ($refreshResponse.success) {
        Write-Host "✓ Token refreshed successfully" -ForegroundColor Green
        $newAccessToken = $refreshResponse.data.accessToken
        Write-Host "  New Access Token received" -ForegroundColor Gray
        Write-Host "  Expires In: $($refreshResponse.data.expiresIn / 1000 / 60 / 60) hours" -ForegroundColor Gray
    } else {
        Write-Host "✗ Token refresh failed" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Refresh token error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 6: Test Invalid Credentials
Write-Host "Test 6: Test Invalid Credentials (Should Fail)" -ForegroundColor Yellow

try {
    $invalidBody = @{
        username = $username
        password = "WrongPassword123!"
    } | ConvertTo-Json

    $invalidResponse = Invoke-RestMethod -Uri "$baseUrl/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $invalidBody `
        -ErrorAction Stop

    Write-Host "✗ Should have failed with invalid credentials" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✓ Correctly rejected invalid credentials" -ForegroundColor Green
    } else {
        Write-Host "✗ Unexpected error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""

# Test 7: Logout
Write-Host "Test 7: Logout" -ForegroundColor Yellow

try {
    $headers = @{
        Authorization = "Bearer $accessToken"
    }

    $logoutResponse = Invoke-RestMethod -Uri "$baseUrl/logout" `
        -Method Post `
        -Headers $headers `
        -ErrorAction Stop

    if ($logoutResponse.success) {
        Write-Host "✓ Logout successful" -ForegroundColor Green
        Write-Host "  Message: $($logoutResponse.data)" -ForegroundColor Gray
    } else {
        Write-Host "✗ Logout failed" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Logout error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 8: Test Refresh After Logout (Should Fail)
Write-Host "Test 8: Test Refresh After Logout (Should Fail)" -ForegroundColor Yellow

try {
    $refreshBody = @{
        refreshToken = $refreshToken
    } | ConvertTo-Json

    $refreshResponse = Invoke-RestMethod -Uri "$baseUrl/refresh" `
        -Method Post `
        -ContentType "application/json" `
        -Body $refreshBody `
        -ErrorAction Stop

    Write-Host "✗ Should have failed with revoked token" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "✓ Correctly rejected revoked refresh token" -ForegroundColor Green
    } else {
        Write-Host "✗ Unexpected error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "All Tests Completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
