$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Invoke-WebRequest -Uri "https://www.gnu.org/licenses/agpl-3.0.txt" -OutFile (Join-Path $Root "LICENSE")
Write-Host "Downloaded official GNU AGPL v3 text."
