source "$(dirname "$0")/common.sh"
$maven -Drevision=$revision spotless:check --no-transfer-progress
