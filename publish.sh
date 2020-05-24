#!/bin/bash

echo Building frontend package
pushd ./frontend
yarn build
popd

echo Building backend package
pushd ./api
sbt stage
sbt docker:stage
sbt docker:publishLocal
popd

echo Publishing package to Heroku
pushd ./api/target/docker/stage/
heroku container:push web -a rock-paper-scissor-battle
heroku container:release web -a rock-paper-scissor-battle
popd

echo Publish complete