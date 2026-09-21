$ErrorActionPreference = 'Stop'
$serverDirectory = $PSScriptRoot
$composeFile = Join-Path $serverDirectory '../deploy/docker-compose.upgrade-test.yml'

Push-Location $serverDirectory
try {
    docker compose -f $composeFile up -d --wait
    if ($LASTEXITCODE -ne 0) { throw 'Test containers failed to start.' }

    # Start the application context once so Flyway initializes the disposable database.
    mvn -B '-Dspring.profiles.active=upgrade-test' '-Dtest=MerchantAiOperationApplicationTests' test
    if ($LASTEXITCODE -ne 0) { throw 'Application context / Flyway bootstrap failed.' }

    Get-Content -Raw 'src/test/resources/upgrade-fixture.sql' |
        docker compose -f $composeFile exec -T mysql mysql -uroot -pboot4-test-only ai_commerce_upgrade_test
    if ($LASTEXITCODE -ne 0) { throw 'Test fixture failed to load.' }

    mvn -B '-Dspring.profiles.active=upgrade-test' verify
    if ($LASTEXITCODE -ne 0) { throw 'Boot 4 regression tests failed. See target/surefire-reports.' }
} finally {
    # Only this explicitly named, disposable Compose project is removed; no business volumes.
    docker compose -f $composeFile down --volumes
    Pop-Location
}
