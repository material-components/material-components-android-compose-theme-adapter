#!/bin/sh

cp README.md docs/index.md
cp CONTRIBUTING.md docs/contributing.md

sed -i 's/CONTRIBUTING.md/\/contributing/' docs/index.md
sed -i 's/docs\/using-snapshot-version.md/using-snapshot-version/' docs/index.md

# Build the docs
./gradlew clean dokkaGfm
