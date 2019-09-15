#!/bin/bash

if ! [[ "$TRAVIS_BRANCH" == "develop" ]]; then exit 0; fi

eval "$(ssh-agent -s)"
chmod 600 $TRAVIS_BUILD_DIR/.travis/id_rsa
ssh-add $TRAVIS_BUILD_DIR/.travis/id_rsa

echo "Hello, world !"

ssh-keyscan -t rsa -H $IP >> ~/.ssh/known_hosts
scp $TRAVIS_BUILD_DIR/target/valyou-*.jar apps@$IP:/opt/valyou-api/

ssh -p $PORT apps@$IP -o StrictHostKeyChecking=no "$( cat <<EOT
    echo "$(date -u) Travis Deploy"  >> ./console.log
    exit
EOT
)"

rm $TRAVIS_BUILD_DIR/.travis/id_rsa