#!/bin/bash

if ! [[ "$TRAVIS_BRANCH" == "develop" ]]; then exit 0; fi

eval "$(ssh-agent -s)"
chmod 600 $TRAVIS_BUILD_DIR/.travis/id_rsa
ssh-add $TRAVIS_BUILD_DIR/.travis/id_rsa
ssh-keyscan -t rsa -H $IP >> ~/.ssh/known_hosts

ssh -p $PORT apps@$IP -o StrictHostKeyChecking=no "$( cat <<EOT
    sudo service valyou stop
    exit
EOT
)"

scp $TRAVIS_BUILD_DIR/bin/valyou.service apps@$IP:$DEPLOY_DIR
scp $TRAVIS_BUILD_DIR/bin/setenv.sh.template apps@$IP:$DEPLOY_DIR
scp $TRAVIS_BUILD_DIR/bin/valyou.sh apps@$IP:$DEPLOY_DIR
scp $TRAVIS_BUILD_DIR/target/valyou-*.jar apps@$IP:$DEPLOY_DIR/valyou.jar

ssh -p $PORT apps@$IP -o StrictHostKeyChecking=no "$( cat <<EOT
    cd $DEPLOY_DIR
    sudo mv valyou.service /etc/systemd/system/valyou.service
    sudo systemctl daemon-reload
    echo "$(date -u) Travis Deploy"  >> ./logs/valyou-api.log
    sudo service valyou start
    exit
EOT
)"

rm $TRAVIS_BUILD_DIR/.travis/id_rsa