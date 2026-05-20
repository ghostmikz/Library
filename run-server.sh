#!/bin/bash
# Library Server — compile and start
cd "$(dirname "$0")"

LIBS="lib/*"
SRC="lib-server/src"
OUT="lib-server/out"

mkdir -p "$OUT"

echo "Compiling server..."
find "$SRC" -name "*.java" > /tmp/lib_server_sources.txt
javac -cp "$LIBS" -d "$OUT" @/tmp/lib_server_sources.txt

if [ $? -eq 0 ]; then
    echo "Starting Library Server on port 9091..."
    java -cp "$OUT:$LIBS" server.LibraryServer
else
    echo "Compilation failed."
    exit 1
fi
