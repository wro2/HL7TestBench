#!/bin/bash
# Build script for HL7 Test Bench (Linux/Mac)
# Requires JDK 17 or higher

set -e

echo "============================================"
echo " HL7 Test Bench Build Script"
echo "============================================"
echo

# Check for Java
if ! command -v javac &> /dev/null; then
    echo "ERROR: javac not found. Please install JDK 17 or higher."
    echo "  Ubuntu/Debian: sudo apt install openjdk-21-jdk"
    echo "  Mac: brew install openjdk@21"
    echo "  Or download from: https://adoptium.net/temurin/releases/"
    exit 1
fi

# Show Java version
echo "Using Java:"
javac -version
echo

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# Create output directories
echo "Creating build directories..."
mkdir -p out/classes
mkdir -p out/jar

# Clean previous build
rm -rf out/classes/*

# Find all Java source files
echo "Compiling source files..."
find src/main/java -name "*.java" > sources.txt

# Compile
if ! javac -d out/classes -sourcepath src/main/java @sources.txt; then
    echo
    echo "ERROR: Compilation failed!"
    rm -f sources.txt
    exit 1
fi
rm -f sources.txt

echo "Compilation successful!"
echo

# Create manifest
echo "Creating JAR manifest..."
echo "Main-Class: com.hl7testbench.HL7TestBench" > out/MANIFEST.MF
echo "" >> out/MANIFEST.MF

# Create JAR
echo "Building JAR file..."
cd out/classes
jar cfm ../jar/HL7TestBench.jar ../MANIFEST.MF .
cd ../..

if [ -f out/jar/HL7TestBench.jar ]; then
    echo
    echo "============================================"
    echo " Build successful!"
    echo "============================================"
    echo
    echo "JAR file created: out/jar/HL7TestBench.jar"
    echo
    echo "To run the application:"
    echo "  java -jar out/jar/HL7TestBench.jar"
    echo
else
    echo "ERROR: Failed to create JAR file"
    exit 1
fi
