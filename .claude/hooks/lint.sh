#!/bin/bash

# PostToolUse hook: Run spotlessApply on project files after Edit/Write/MultiEdit

INPUT=$(cat)

# Extract file_path from tool_input
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')

# Check if it's a file type that Spotless handles
case "$FILE_PATH" in
  *.java|*.json|*.yaml|*.yml|*.md|*.properties|*.xml|*.sql|*.sh)
    echo "[lint hook] Running spotlessApply on: $FILE_PATH"
    cd "$CLAUDE_PROJECT_DIR" || exit 0
    OUTPUT=$(./gradlew spotlessApply -q 2>&1)
    EXIT_CODE=$?
    if [ "$EXIT_CODE" -ne 0 ]; then
      echo "[lint hook] spotlessApply failed (exit $EXIT_CODE):" >&2
      echo "$OUTPUT" >&2
    fi
    ;;
esac

exit 0
