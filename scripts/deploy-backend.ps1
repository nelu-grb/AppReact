[CmdletBinding()]
param(
    [ValidateSet("Local", "Aws", "Azure")]
    [string]$Target,
    [ValidateSet("Start", "Stop")]
    [string]$Action = "Start",
    [ValidatePattern("^[a-zA-Z0-9][a-zA-Z0-9-]{1,30}$")]
    [string]$ProjectName = "andesstay",
    [string]$AwsRegion = "us-east-1",
    [string]$AzureResourceGroup = "",
    [string]$AzureLocation = "eastus",
    [switch]$WhatIf
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

function Read-Choice {
    Write-Host "Seleccione el destino de despliegue:"
    Write-Host "  1. Local (Docker Compose)"
    Write-Host "  2. AWS (ECR/ECS)"
    Write-Host "  3. Azure (ACR/Container Apps)"
    $choice = Read-Host "Opcion"
    switch ($choice) {
        "1" { return "Local" }
        "2" { return "Aws" }
        "3" { return "Azure" }
        default { throw "Destino invalido. Use 1, 2 o 3." }
    }
}

if (-not $Target) { $Target = Read-Choice }
if ($Target -eq "Local" -and -not $WhatIf -and $Action -eq "Start" -and $PSBoundParameters.Count -eq 0) {
    Write-Host "`nSeleccione la operacion local:"
    Write-Host "  1. Iniciar backend"
    Write-Host "  2. Apagar backend"
    $operation = Read-Host "Opcion"
    switch ($operation) {
        "1" { $Action = "Start" }
        "2" { $Action = "Stop" }
        default { throw "Operacion invalida. Use 1 o 2." }
    }
}
if ($Target -eq "Azure" -and [string]::IsNullOrWhiteSpace($AzureResourceGroup)) {
    $AzureResourceGroup = "$ProjectName-rg"
}

function Assert-Command($command) {
    if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
        throw "No se encontro '$command'. Instale la herramienta requerida y vuelva a intentar."
    }
}

switch ($Target) {
    "Local" {
        Assert-Command "docker"
        if ($Action -eq "Stop") {
            $stopCommands = @(
                "docker compose down",
                "docker compose -f docker-compose.messaging.yml down"
            )
            Write-Host "`nComandos de apagado:"
            $stopCommands | ForEach-Object { Write-Host "  $_" }
            if ($WhatIf) {
                Write-Host "`nModo WhatIf: no se ejecutaron comandos."
                break
            }
            Write-Host "`nApagando microservicios..."
            docker compose down
            if ($LASTEXITCODE -ne 0) {
                throw "No se pudieron detener los microservicios."
            }
            Write-Host "Apagando mensajeria..."
            docker compose -f docker-compose.messaging.yml down
            if ($LASTEXITCODE -ne 0) {
                throw "No se pudo detener la infraestructura de mensajeria."
            }
            Write-Host "`nBackend local apagado. Los volumenes no fueron eliminados."
            break
        }
        $commands = @(
            "docker compose -f docker-compose.messaging.yml up -d",
            "docker compose up -d --build"
        )
        Write-Host "`nComandos del despliegue local:"
        $commands | ForEach-Object { Write-Host "  $_" }
        if (-not $WhatIf) {
            Write-Host "`nIniciando despliegue local..."
            docker compose -f docker-compose.messaging.yml up -d
            if ($LASTEXITCODE -ne 0) {
                throw "No se pudo iniciar la infraestructura de mensajeria."
            }
            docker compose up -d --build
            if ($LASTEXITCODE -ne 0) {
                throw "No se pudo construir o iniciar los microservicios."
            }
            Write-Host "`nDespliegue local completado."
        } else {
            Write-Host "`nModo WhatIf: no se ejecutaron comandos."
        }
    }
    "Aws" {
        Assert-Command "aws"
        Assert-Command "docker"
        aws sts get-caller-identity | Out-Host
        Write-Host "`nAWS validado. Region: $AwsRegion"
        Write-Host "El siguiente paso requiere definir ECR, ECS, VPC, secretos y dominios del entorno."
        Write-Host "Use la salida de este wizard como checklist y no ejecute comandos cloud sin revisar sus parametros."
    }
    "Azure" {
        Assert-Command "az"
        Assert-Command "docker"
        az account show | Out-Host
        Write-Host "`nAzure validado. Resource group: $AzureResourceGroup; Location: $AzureLocation"
        Write-Host "El siguiente paso requiere definir ACR, Container Apps, red, secretos y dominios del entorno."
        Write-Host "Use la salida de este wizard como checklist y no ejecute comandos cloud sin revisar sus parametros."
    }
}
