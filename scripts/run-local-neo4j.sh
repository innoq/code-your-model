#!/bin/sh

PROJECT_ROOT="`cd "${0%/*}/.."; pwd`"

docker run \
    -p7474:7474 -p7687:7687 \
    -d \
    -v $PROJECT_ROOT/neo4j/data:/data \
    -v $PROJECT_ROOT/neo4j/logs:/logs \
    -v $PROJECT_ROOT/neo4j/import:/var/lib/neo4j/import \
    -v $PROJECT_ROOT/neo4j/plugins:/plugins \
    --env NEO4J_AUTH=neo4j/test \
    neo4j:3.5.12
