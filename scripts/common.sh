revision=$(git log -1 --format=%h)
maven="mvn"
if command -v mvnd &>/dev/null; then
  maven="mvnd"
fi
