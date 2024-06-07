#!/bin/bash

# Start script for item-group-workflow-api

PORT=8080
exec java -jar -Dserver.port="${PORT}" "item-group-workflow-api.jar"
