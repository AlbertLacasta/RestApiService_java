#!/usr/bin/env bash

cd RestApiService_java

# discard local changes
git reset --hard HEAD

# pull
git pull

# Remove unecesari things
rm -rf src/main/java/com/moock

# run
mvn spring-boot:run
