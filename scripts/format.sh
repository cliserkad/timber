source "$(dirname "$0")/common.sh"
$maven -Drevision=$revision spotless:apply --no-transfer-progress
