#!/bin/bash

# Compile first to be safe
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

find_java_exe() {
    local exe="$1"
    if command -v "$exe" >/dev/null 2>&1; then
        command -v "$exe"
        return 0
    fi
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/$exe" ]; then
        echo "$JAVA_HOME/bin/$exe"
        return 0
    fi
    for candidate in /usr/lib/jvm/*/bin/"$exe" /usr/java/*/bin/"$exe"; do
        if [ -x "$candidate" ]; then
            echo "$candidate"
            return 0
        fi
    done
    return 1
}

JAVAC_BIN="$(find_java_exe javac)"
JAVA_BIN="$(find_java_exe java)"

if [ -z "$JAVAC_BIN" ] || [ -z "$JAVA_BIN" ]; then
    echo "Java not found. Please install a JDK (11+) or set JAVA_HOME."
    exit 1
fi

echo "Compiling JIHLL..."
"$JAVAC_BIN" -d "$ROOT_DIR/bin" "$ROOT_DIR"/src/com/jihll/*.java

if [ $? -ne 0 ]; then
    echo "Compilation Failed!"
    exit 1
fi

echo "--------------------------------------"
echo "Running Verification Suite"
echo "--------------------------------------"

# Define the Java Command
JAVA_CMD="$JAVA_BIN -cp $ROOT_DIR/bin com.jihll.JihllLanguage"

# Run each test
$JAVA_CMD tests/test_core.jihll
echo ""
$JAVA_CMD tests/test_functions.jihll
echo ""
$JAVA_CMD tests/test_data.jihll
echo ""
$JAVA_CMD tests/test_io.jihll
echo ""
$JAVA_CMD tests/test_concurrency.jihll
echo ""
$JAVA_CMD tests/test_modules.jihll

echo "--------------------------------------"
echo "Cleaning up..."
rm test_file.txt 2>/dev/null
echo "Done."