#!/bin/bash
set -e
echo "Testing Postgres..."
docker exec pokemon-postgres pg_isready -U pokemon
echo "Testing MongoDB..."
docker exec pokemon-mongo mongosh --eval "db.adminCommand('ping')" --quiet
echo "Testing Redis..."
docker exec pokemon-redis redis-cli ping
echo "All infra OK"
