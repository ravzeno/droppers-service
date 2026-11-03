#!/bin/bash

echo '🔍 Extracting version and app name from build.gradle.kts...'

# Get version and app name from build.gradle.kts
version=$(grep "^version" build.gradle.kts | cut -d' ' -f3 | tr -d '"')
appname=$(grep "^group" build.gradle.kts | cut -d' ' -f3 | tr -d '"')

echo '🔍 Getting git commit hash...'

# Get git commit hash
commit=$(git rev-parse --short HEAD)

echo '🔍 Getting git branch...'

# Get git branch
branch=$(git rev-parse --abbrev-ref HEAD)

echo '🔍 Creating JSON...'

# Create JSON
json=$(printf '{"version": "%s", "name": "%s", "gitCommit": "%s", "gitBranch": "%s"}' "$version" "$appname" "$commit" "$branch")

echo '📝 Writing JSON to file in the main resources folder...'

# Write JSON to file in the main resources folder
echo $json > src/main/resources/app-info.json

echo '✅ Done!'