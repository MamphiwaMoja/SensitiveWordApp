param(
    [string] $CsvPath = "target/site/jacoco/jacoco.csv",
    [string] $BadgePath = ".github/badges/coverage.svg",
    [ValidateSet("Instruction", "Line")]
    [string] $Metric = "Instruction",
    [switch] $Verify
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $CsvPath)) {
    throw "JaCoCo CSV report was not found at '$CsvPath'. Run 'mvn clean test jacoco:report' first."
}

$rows = Import-Csv -LiteralPath $CsvPath

if ($Metric -eq "Line") {
    $missedColumn = "LINE_MISSED"
    $coveredColumn = "LINE_COVERED"
    $titleMetric = "line"
} else {
    $missedColumn = "INSTRUCTION_MISSED"
    $coveredColumn = "INSTRUCTION_COVERED"
    $titleMetric = "instruction"
}

[long] $missed = 0
[long] $covered = 0

foreach ($row in $rows) {
    $missed += [long] $row.$missedColumn
    $covered += [long] $row.$coveredColumn
}

$total = $missed + $covered
if ($total -eq 0) {
    $percentage = 0
} else {
    $percentage = ($covered * 100.0) / $total
}

$coverage = $percentage.ToString("0.00", [System.Globalization.CultureInfo]::InvariantCulture)
$displayCoverage = "$coverage%"

if ($percentage -ge 90) {
    $color = "#4c1"
} elseif ($percentage -ge 75) {
    $color = "#97ca00"
} elseif ($percentage -ge 60) {
    $color = "#dfb317"
} else {
    $color = "#e05d44"
}

$svg = @"
<svg xmlns="http://www.w3.org/2000/svg" width="112" height="20" role="img" aria-label="coverage: $displayCoverage">
  <title>$titleMetric coverage: $displayCoverage</title>
  <linearGradient id="s" x2="0" y2="100%">
    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
    <stop offset="1" stop-opacity=".1"/>
  </linearGradient>
  <clipPath id="r">
    <rect width="112" height="20" rx="3" fill="#fff"/>
  </clipPath>
  <g clip-path="url(#r)">
    <rect width="63" height="20" fill="#555"/>
    <rect x="63" width="49" height="20" fill="$color"/>
    <rect width="112" height="20" fill="url(#s)"/>
  </g>
  <g fill="#fff" text-anchor="middle" font-family="Verdana,Geneva,DejaVu Sans,sans-serif" font-size="11">
    <text x="31.5" y="15" fill="#010101" fill-opacity=".3">coverage</text>
    <text x="31.5" y="14">coverage</text>
    <text x="86.5" y="15" fill="#010101" fill-opacity=".3">$displayCoverage</text>
    <text x="86.5" y="14">$displayCoverage</text>
  </g>
</svg>
"@

$svg = $svg.TrimEnd() + "`n"

if ($Verify) {
    if (-not (Test-Path -LiteralPath $BadgePath)) {
        throw "Coverage badge was not found at '$BadgePath'."
    }

    $existing = Get-Content -LiteralPath $BadgePath -Raw
    if ($existing -ne $svg) {
        throw "Coverage badge is stale. Expected $displayCoverage ($titleMetric coverage). Run './scripts/update-coverage-badge.ps1' after generating the JaCoCo report."
    }

    Write-Host "Coverage badge is up to date: $displayCoverage ($titleMetric coverage)."
    exit 0
}

$badgeDirectory = Split-Path -Parent $BadgePath
if ($badgeDirectory -and -not (Test-Path -LiteralPath $badgeDirectory)) {
    New-Item -ItemType Directory -Path $badgeDirectory | Out-Null
}

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($BadgePath, $svg, $utf8NoBom)
Write-Host "Updated coverage badge to $displayCoverage ($titleMetric coverage)."
