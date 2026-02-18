$logPath = Join-Path $PSScriptRoot "Logs"
$files = @(
    "$logPath\backend.log",
    "$logPath\frontend.log",
    "$logPath\analytics.log"
)

Write-Host "Monitoring logs from: $logPath"
Write-Host "Waiting for log files to be created..."

# Wait for at least one file to exist before starting
while ($true) {
    try {
        $existing = $files | Where-Object { Test-Path $_ }
        if ($existing.Count -gt 0) { 
            Write-Host "Found $($existing.Count) log files. Starting monitor..."
            break 
        }
    }
    catch {
        # Ignore errors during check
    }
    Start-Sleep -Seconds 2
}

Get-Content -Path $files -Wait -Tail 10 -ErrorAction SilentlyContinue
