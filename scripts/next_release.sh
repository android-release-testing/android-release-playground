#!/usr/bin/env bash
set -euo pipefail

NEXT_VERSION=$1

git checkout master &> /dev/null
echo "$NEXT_VERSION" > versioning/version
git commit -am "Bump to $NEXT_VERSION" &> /dev/null
git push origin master &> /dev/null

RELEASE_BRANCH="release/$NEXT_VERSION"
git checkout -b "$RELEASE_BRANCH" &> /dev/null
echo "50" > versioning/rc
git commit -am "Set RC to 50" &> /dev/null
git push origin "$RELEASE_BRANCH" &> /dev/null

REVISION=$(git rev-parse HEAD)
echo "$REVISION" | pbcopy
echo "✅ Created $RELEASE_BRANCH"
echo "🔗 Revision (copied to clipboard): $REVISION"

git checkout master &> /dev/null
