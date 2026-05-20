source "$(dirname "$0")/common.sh"
$maven -Drevision=$revision -B clean deploy --no-transfer-progress
