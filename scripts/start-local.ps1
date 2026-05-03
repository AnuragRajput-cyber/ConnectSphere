$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$logs = Join-Path $root "logs"
New-Item -ItemType Directory -Force -Path $logs | Out-Null

$services = @(
  @{ Name = "service-registry"; Port = 8761 },
  @{ Name = "auth-service"; Port = 8081 },
  @{ Name = "post-service"; Port = 8082 },
  @{ Name = "comment-service"; Port = 8083 },
  @{ Name = "like-service"; Port = 8084 },
  @{ Name = "follow-service"; Port = 8085 },
  @{ Name = "notification-service"; Port = 8086 },
  @{ Name = "media-service"; Port = 8087 },
  @{ Name = "search-service"; Port = 8088 },
  @{ Name = "chat-service"; Port = 8089 },
  @{ Name = "api-gateway"; Port = 8080 }
)

function Stop-PortOwner([int]$Port) {
  $connections = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue
  foreach ($connection in $connections) {
    $processId = $connection.OwningProcess
    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if ($process -and $process.Path -like "*\Java\jdk-17\bin\java.exe") {
      Write-Host "Stopping existing process $processId on port $Port"
      Stop-Process -Id $processId -Force
    }
  }
}

function Wait-Health([string]$Name, [int]$Port) {
  $deadline = (Get-Date).AddSeconds(120)
  do {
    try {
      $response = Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:$Port/actuator/health" -TimeoutSec 5
      if ($response.StatusCode -eq 200) {
        Write-Host "$Name is UP on $Port"
        return
      }
    } catch {
      Start-Sleep -Seconds 2
    }
  } while ((Get-Date) -lt $deadline)

  Write-Warning "$Name did not report UP within 120 seconds. Check logs/$Name.log"
}

foreach ($service in $services) {
  Stop-PortOwner $service.Port
}

foreach ($service in $services) {
  $name = $service.Name
  $port = $service.Port
  $cwd = Join-Path $root $name
  $log = Join-Path $logs "$name.log"
  Write-Host "Starting $name on $port"
  Start-Process -FilePath "powershell" `
    -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", "mvn spring-boot:run *> `"$log`"") `
    -WorkingDirectory $cwd `
    -WindowStyle Hidden | Out-Null
  Wait-Health $name $port
}

Write-Host "Backend services started. Start the Angular app with: cd connectsphere-web; npm start"
