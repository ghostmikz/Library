#!/bin/bash
# Library Client — compile and start
cd "$(dirname "$0")"

# Prefer JetBrains Runtime for HiDPI Wayland support
JBR_HOME="$HOME/.local/share/JetBrains/Toolbox/apps/intellij-idea/jbr"
if [ -x "$JBR_HOME/bin/java" ]; then
    JAVA="$JBR_HOME/bin/java"
    JAVAC="$JBR_HOME/bin/javac"
else
    JAVA="java"
    JAVAC="javac"
fi

LIBS="lib/*"
SRC="lib-client/src"
OUT="lib-client/out"

mkdir -p "$OUT"

echo "Compiling client..."
find "$SRC" -name "*.java" > /tmp/lib_client_sources.txt
"$JAVAC" -cp "$LIBS" -d "$OUT" @/tmp/lib_client_sources.txt

if [ $? -eq 0 ]; then
    # Copy resources (properties, images) to output
    find "$SRC" \( -name "*.properties" -o -name "*.png" -o -name "*.jpg" -o -name "*.gif" \) | while read f; do
        rel="${f#$SRC/}"
        mkdir -p "$OUT/$(dirname "$rel")"
        cp "$f" "$OUT/$rel"
    done

    echo "Starting Library Client..."
    "$JAVA" \
        -Dawt.useSystemAAFontSettings=lcd \
        -Dswing.aatext=true \
        -cp "$OUT:$LIBS" App
else
    echo "Compilation failed."
    exit 1
fi
