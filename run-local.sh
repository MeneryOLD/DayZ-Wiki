#!/usr/bin/env bash
# Loads .env and starts the DayZ Wiki jar against local MySQL.
cd "$(dirname "$0")"
set -a; . ./.env; set +a
exec java -jar target/dayz-wiki-0.0.1-SNAPSHOT.jar --spring.profiles.active=docker
