source "$(dirname "$0")/common.sh"
$maven -Drevision=$revision verify
