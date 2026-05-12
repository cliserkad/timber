source "$(dirname "$0")/common.sh"
$maven -Drevision=$revision clean install --no-transfer-progress
