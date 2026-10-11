param(
    [string]$BaseUrl = 'http://localhost:8080/api/v1',
    [string]$ManagerUsername = 'Manager',
    [string]$ManagerPassword = 'Manager123'
)

$ErrorActionPreference = 'Stop'
$endpoint = [Uri]$BaseUrl
if (-not $endpoint.IsLoopback -or $endpoint.Scheme -notin @('http', 'https')) {
    throw 'This script creates documented demo passwords. Use a local demo server only.'
}
$BaseUrl = $BaseUrl.TrimEnd('/')
$login = @{ username = $ManagerUsername; password = $ManagerPassword } | ConvertTo-Json
$actor = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method Post -ContentType 'application/json' -Body $login -SessionVariable demoSession
try {
    if ($actor.role -ne 'MANAGER') { throw 'A Manager account is required to create demo users.' }
    $accounts = @(
        @{ username = 'Manager'; password = 'Manager123'; role = 'MANAGER'; lastName = 'Manager' },
        @{ username = 'Supervisor'; password = 'Supervisor123'; role = 'SUPERVISOR'; lastName = 'Supervisor' },
        @{ username = 'ServiceStaff'; password = 'ServiceStaff123'; role = 'SERVICE_STAFF'; lastName = 'ServiceStaff' },
        @{ username = 'KitchenStaff'; password = 'KitchenStaff123'; role = 'KITCHEN_STAFF'; lastName = 'KitchenStaff' }
    )
    $existing = Invoke-RestMethod -Uri "$BaseUrl/admin/users" -WebSession $demoSession
    $archived = Invoke-RestMethod -Uri "$BaseUrl/admin/users/archived" -WebSession $demoSession
    $knownAccounts = @($existing) + @($archived)
    foreach ($account in $accounts) {
        $found = @($knownAccounts | Where-Object { $_.username -ceq $account.username })
        if ($found.Count -gt 0) {
            if ($found[0].role -ne $account.role -or -not $found[0].active -or $null -ne $found[0].archivedAt) {
                throw "Existing $($account.username) has a different role or is disabled/archived. No account was overwritten."
            }
            # Verify the published credentials without replacing an existing password.
            $check = @{ username = $account.username; password = $account.password } | ConvertTo-Json
            $verified = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method Post -ContentType 'application/json' -Body $check -SessionVariable checkSession
            try { if ($verified.role -ne $account.role) { throw 'Unexpected demo account role.' } }
            finally { Invoke-RestMethod -Uri "$BaseUrl/auth/logout" -Method Post -WebSession $checkSession | Out-Null }
            Write-Output "$($account.username): verified existing $($account.role) account"
            continue
        }
        $body = @{
            username = $account.username; password = $account.password; role = $account.role
            displayName = "[TEST DATA] $($account.lastName)"; email = "$($account.username.ToLowerInvariant())@example.test"
            firstName = 'Demo'; lastName = $account.lastName; phoneNumber = $null
        } | ConvertTo-Json
        Invoke-RestMethod -Uri "$BaseUrl/admin/users" -Method Post -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($body)) -WebSession $demoSession | Out-Null
        Write-Output "$($account.username): created $($account.role) through Manager user administration"
    }
}
finally {
    Invoke-RestMethod -Uri "$BaseUrl/auth/logout" -Method Post -WebSession $demoSession | Out-Null
}
