#!/bin/bash

docker-compose down
docker-compose build --no-cache
docker compose -f docker-compose-lgtm.yml -f docker-compose.yml up -d
