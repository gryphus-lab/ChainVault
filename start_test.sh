#!/bin/bash

# Configuration
GET_URL="http://localhost:9091/documents"
POST_URL="http://localhost:8085/chainvault/process"
CONTENT_TYPE="application/json"

# 1. Fetch JSON and parse the array of IDs
# -s suppresses progress; -r gives raw output (no quotes)
ids=$(curl -s "$GET_URL" | jq -r '.[].id')

# Check if any IDs were found
if [ -z "$ids" ]; then
  echo "No IDs found or error parsing JSON."
  exit 1
fi

# 2. Loop through each ID and make a POST request
for id in $ids; do
  echo "Processing ID: $id"

  # Send POST request with ID in the body
  curl -s -X POST "$POST_URL" \
       -H "Content-Type: $CONTENT_TYPE" \
       -d "{\"docId\": \"$id\"}"

  echo -e "\nDone with $id\n"
done
