<##>
# Sends README.md to http://localhost:8080 via POST (PowerShell)
param(
    [string]$TargetUrl = 'http://localhost:8080',
    [string]$FilePath = 'README.md'
)

if (-not (Test-Path -Path $FilePath)) {
    Write-Error "File '$FilePath' not found in current directory."
    exit 1
}

Write-Host "Posting $FilePath to $TargetUrl"

# Use Invoke-RestMethod for simple POST; send raw file bytes and proper content-type
$bytes = [System.IO.File]::ReadAllBytes((Resolve-Path $FilePath))

[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12

$response = Invoke-RestMethod -Uri $TargetUrl -Method Post -Body $bytes -ContentType 'text/markdown'

Write-Host "Response:`n" $response

Exit 0
