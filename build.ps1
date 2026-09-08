$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
Set-Location -LiteralPath $PSScriptRoot
try {
    if ($env:JAVA_HOME) { $env:PATH = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:PATH }
    if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
        throw 'Install JDK 21 (not just JRE) from https://adoptium.net/temurin/releases/?version=21 and set JAVA_HOME.'
    }
    $version = (& javac -version 2>&1 | Out-String)
    if ($version -notmatch 'javac 21(?:\.|\s|$)') { throw "JDK 21 is required. Detected: $version" }
    $gradleVersion = '9.2.1'
    $tools = Join-Path $PSScriptRoot '.tools'
    $gradle = Join-Path $tools "gradle-$gradleVersion\bin\gradle.bat"
    if (-not (Test-Path -LiteralPath $gradle)) {
        New-Item -ItemType Directory -Force -Path $tools | Out-Null
        $url = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
        $zip = Join-Path $tools "gradle-$gradleVersion-bin.zip"
        Write-Host 'Downloading Gradle from the official distribution server...'
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $zip
        $checksum = (Invoke-WebRequest -UseBasicParsing -Uri "$url.sha256").Content.Trim()
        if ($checksum -notmatch '^[a-fA-F0-9]{64}$') { throw 'Invalid Gradle checksum response.' }
        $actual = (Get-FileHash -LiteralPath $zip -Algorithm SHA256).Hash
        if ($actual -ine $checksum) { Remove-Item -LiteralPath $zip; throw 'Gradle SHA-256 mismatch. Download was not executed.' }
        Expand-Archive -LiteralPath $zip -DestinationPath $tools -Force
        Remove-Item -LiteralPath $zip
    }
    & $gradle --no-daemon --console=plain clean build
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed (exit code $LASTEXITCODE). See the errors above." }
    Write-Host "`nSUCCESS: build\libs\luma-visuals-0.1.0+mc1.21.11.jar" -ForegroundColor Green
    Write-Host 'Install only the normal JAR, not the sources JAR. Fabric API is also required.'
} catch {
    Write-Host "`nBUILD FAILED: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
