revision=$(git log -1 --format=%ct)
mvnd -Drevision=$revision clean verify
