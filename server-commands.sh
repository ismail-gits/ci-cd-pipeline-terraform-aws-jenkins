#!/usr/bin/env bash

export IMAGE=$1
export DOCKER_USER=$2
export DOCKER_PASSWORD=$3

echo $DOCKER_PASSWORD | docker login -u $DOCKER_USER --password-stdin
docker-compose -f docker-compose.yaml up --detach
echo "all containers are successfully up and running..."