#!/usr/bin/env bash
# Sends README.md to http://localhost:8080 via POST
set -euo pipefail

TARGET_URL="http://localhost:8080"
FILE_PATH="README.md"

if [ ! -f "$FILE_PATH" ]; then
  echo "File '$FILE_PATH' not found in current directory."
  exit 1
fi

echo "Posting $FILE_PATH to $TARGET_URL"
curl -v -X POST "$TARGET_URL" \
  -H "Content-Type: text/markdown" \
  --data-binary @"$FILE_PATH"

echo "Done."
