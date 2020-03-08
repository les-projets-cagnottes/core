#!/bin/bash

TAG_NAME=$1
DEPLOY_LOCATION="$( cd "$(dirname "$0")" ; pwd -P )"/..
FINAL_LOCATION=/opt/les-projets-cagnottes/core

echo "$(date -u) Automatic Deploy"  >> ./console.log

sudo service les-projets-cagnottes-core stop
sleep 1

mkdir -p ${FINAL_LOCATION}/${TAG_NAME}
sudo cp ${DEPLOY_LOCATION}/bin/les-projets-cagnottes-core.service /etc/systemd/system/les-projets-cagnottes-core.service
cp ${DEPLOY_LOCATION}/bin/setenv.sh.template ${FINAL_LOCATION}/setenv.template
cp ${DEPLOY_LOCATION}/bin/les-projets-cagnottes-core.sh ${FINAL_LOCATION}/les-projets-cagnottes-core.sh
cp ${DEPLOY_LOCATION}/target/core-${TAG_NAME}.jar ${FINAL_LOCATION}/les-projets-cagnottes-core.jar
cp -R ${DEPLOY_LOCATION} ${FINAL_LOCATION}/${TAG_NAME}

sudo systemctl daemon-reload
sudo service les-projets-cagnottes-core start

exit 0
