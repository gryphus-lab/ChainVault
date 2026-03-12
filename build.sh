#!/bin/bash

docker compose -p chainvault down -v
mvn clean verify -Pcoverage