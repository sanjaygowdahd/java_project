$libDir = "lib"
if (!(Test-Path -Path $libDir)) {
    New-Item -ItemType Directory -Path $libDir | Out-Null
}

$sqliteUrl = "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar"
$flatlafUrl = "https://repo1.maven.org/maven2/com/formdev/flatlaf/3.4/flatlaf-3.4.jar"
$slf4jUrl = "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar"
$slf4jSimpleUrl = "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar"

$sqliteFile = "$libDir\sqlite-jdbc-3.45.1.0.jar"
$flatlafFile = "$libDir\flatlaf-3.4.jar"
$slf4jFile = "$libDir\slf4j-api-1.7.36.jar"
$slf4jSimpleFile = "$libDir\slf4j-simple-1.7.36.jar"

if (!(Test-Path -Path $sqliteFile)) {
    Write-Host "Downloading sqlite-jdbc..."
    Invoke-WebRequest -Uri $sqliteUrl -OutFile $sqliteFile
}
if (!(Test-Path -Path $flatlafFile)) {
    Write-Host "Downloading flatlaf..."
    Invoke-WebRequest -Uri $flatlafUrl -OutFile $flatlafFile
}
if (!(Test-Path -Path $slf4jFile)) {
    Write-Host "Downloading slf4j-api..."
    Invoke-WebRequest -Uri $slf4jUrl -OutFile $slf4jFile
}
if (!(Test-Path -Path $slf4jSimpleFile)) {
    Write-Host "Downloading slf4j-simple..."
    Invoke-WebRequest -Uri $slf4jSimpleUrl -OutFile $slf4jSimpleFile
}

Write-Host "Compiling..."
javac -cp "lib/*" HotelBookingApp.java

if ($?) {
    Write-Host "Running..."
    java -cp ".;lib/*" HotelBookingApp
} else {
    Write-Host "Compilation failed."
}
