#!/usr/bin/env bash
# Bundles the project's source into a single text file to share with Claude.
set -e
OUT="${1:-tenderpilot-context.txt}"

EXCLUDES=( -not -path "*/.git/*" -not -path "*/node_modules/*" -not -path "*/target/*" \
  -not -path "*/build/*" -not -path "*/.next/*" -not -path "*/dist/*" -not -path "*/.idea/*" \
  -not -path "*/.vscode/*" -not -path "*/.mvn/*" -not -path "*/uploads/*" \
  -not -name ".DS_Store" -not -name "*.log" -not -name "package-lock.json" \
  -not -name "*.min.*" -not -name "$OUT" -not -name ".env*" )

{
  echo "##### PROJECT TREE #####"
  find . -type f "${EXCLUDES[@]}" | sort
  echo
  echo "##### FILE CONTENTS #####"
  find . -type f \( -name "*.java" -o -name "*.kt" -o -name "pom.xml" -o -name "*.gradle" \
    -o -name "*.properties" -o -name "*.yml" -o -name "*.yaml" -o -name "*.sql" \
    -o -name "*.html" -o -name "*.css" -o -name "*.scss" -o -name "*.js" -o -name "*.jsx" \
    -o -name "*.ts" -o -name "*.tsx" -o -name "*.json" -o -name "*.md" -o -name "*.sh" \
    -o -name "Dockerfile" -o -name "docker-compose*" \) "${EXCLUDES[@]}" | sort | while IFS= read -r f; do
    echo; echo "===== FILE: $f ====="
    case "$f" in
      *.properties|*.yml|*.yaml)
        # mask passwords / secrets / keys in config files
        perl -pe 's/^(.*(password|secret|token|api[-_.]?key|private[-_.]?key|credentials).*?[=:]\s*).*/$1***REDACTED***/i' "$f" ;;
      *) cat "$f" ;;
    esac
  done
} > "$OUT"

echo "Done: $OUT ($(wc -l < "$OUT") lines, $(du -h "$OUT" | cut -f1))"
