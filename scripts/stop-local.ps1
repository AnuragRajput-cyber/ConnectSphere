$ErrorActionPreference = "Stop"

$ports = @(8080, 8081, 8082, 8083, 8084, 8085, 8086, 8087, 8088, 8089, 8761)

foreach ($port in $ports) {
  $connections = Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue
  foreach ($connection in $connections) {
    $processId = $connection.OwningProcess
    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if ($process -and $process.Path -like "*\Java\jdk-17\bin\java.exe") {
      Write-Host "Stopping $($process.ProcessName) $processId on port $port"
      Stop-Process -Id $processId -Force
    }
  }
}
