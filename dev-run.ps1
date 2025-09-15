Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Set-EnvFromDotenv {
  param([string]$Path = '.env')
  if (-Not (Test-Path $Path)) { return }
  Write-Host "Loading env from $Path" -ForegroundColor Cyan
  Get-Content $Path | ForEach-Object {
    $line = $_.Trim()
    if (-not $line) { return }
    if ($line.StartsWith('#')) { return }
    $idx = $line.IndexOf('=')
    if ($idx -lt 1) { return }
    $key = $line.Substring(0, $idx).Trim()
    $val = $line.Substring($idx + 1).Trim()
    # Remove optional quotes
    if (($val.StartsWith('"') -and $val.EndsWith('"')) -or ($val.StartsWith("'") -and $val.EndsWith("'"))) {
      $val = $val.Substring(1, $val.Length - 2)
    }
    [System.Environment]::SetEnvironmentVariable($key, $val, 'Process')
  }
}

Push-Location $PSScriptRoot
try {
  Set-EnvFromDotenv '.env'

  if (-not $env:PORTFOLIO_ADMIN_SECURITY_ENABLED) {
    $env:PORTFOLIO_ADMIN_SECURITY_ENABLED = 'true'
  }

  $tokenLen = ($env:PORTFOLIO_ADMIN_TOKEN ?? '').Length
  Write-Host "Admin security: $env:PORTFOLIO_ADMIN_SECURITY_ENABLED, token length: $tokenLen" -ForegroundColor Green

  if (-not (Test-Path './mvnw')) {
    Write-Error 'mvnw not found. Run from portfolio-backend directory.'
    exit 1
  }

  ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
}
finally {
  Pop-Location
}

