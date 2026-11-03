#!/bin/bash

# Setup file path
release_date=$(date +"%Y-%m-%d %H:%M:%S")

# Prompt user to enter version number
echo "🚀 Enter the version number (e.g., 1.0.0):"
read version

change_log_base_dir="./changelog"
change_log_file="${version}.md"
changelog_path="${change_log_base_dir}/${change_log_file}"

# Ensure the directory exists and is writable
mkdir -p "$change_log_base_dir"

# Check if file already exists
if [ -f "$changelog_path" ]; then
    # File exists, prompt user for confirmation
    read -p "❓ Changelog file for version ${version} already exists. Do you want to overwrite? (y/n): " overwrite_confirm
    if [ "$overwrite_confirm" != "y" ]; then
        echo "ℹ️ Operation cancelled. No changes made."
        exit 0
    fi
fi

# Store the changelog template into a variable
changelog_content=$(cat <<EOL
# [${version}] - ${release_date}

## Added

- 

## Changed

- 

## Fixed

- 

## Deprecated

-

## Removed

- 
EOL
)

# Create or overwrite the release notes file
echo "$changelog_content" > "$changelog_path"

echo "🚀 Changelog file created or updated: ${changelog_path}"
