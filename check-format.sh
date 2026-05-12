revision=$(git log -1 --format=%ct)
maven="mvn"
if command -v mvnd &>/dev/null; then
  maven="mvnd"
fi
$maven -Drevision=$revision spotless:check --no-transfer-progress
