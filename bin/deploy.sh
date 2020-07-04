#!/bin/bash

TAG_NAME=$1
DEPLOY_LOCATION="$( cd "$(dirname "$0")" ; pwd -P )"/..
FINAL_LOCATION=/opt/les-projets-cagnottes/core

echo "$(date -u) Automatic Deploy"  >> ./console.log

sudo service les-projets-cagnottes-core stop
sleep 1

# Backup environment config
cp ${FINAL_LOCATION}/current/setenv.sh ${DEPLOY_LOCATION}/setenv.sh
cp ${FINAL_LOCATION}/current/application.properties ${DEPLOY_LOCATION}/application.properties

# Update systemd
sudo cp ${DEPLOY_LOCATION}/bin/les-projets-cagnottes-core.service /etc/systemd/system/les-projets-cagnottes-core.service

# Create folder for version
mkdir -p ${FINAL_LOCATION}/${TAG_NAME}
cp ${DEPLOY_LOCATION}/bin/setenv.sh.template ${FINAL_LOCATION}/${TAG_NAME}/setenv.template
cp ${DEPLOY_LOCATION}/bin/les-projets-cagnottes-core.sh ${FINAL_LOCATION}/${TAG_NAME}/les-projets-cagnottes-core.sh
cp ${DEPLOY_LOCATION}/target/core-${TAG_NAME}.jar ${FINAL_LOCATION}/${TAG_NAME}/les-projets-cagnottes-core.jar
cp ${DEPLOY_LOCATION}/setenv.sh ${FINAL_LOCATION}/${TAG_NAME}/setenv.sh
cp ${DEPLOY_LOCATION}/application.properties ${FINAL_LOCATION}/${TAG_NAME}/application.properties

# Update current symlink
ln -sf ${FINAL_LOCATION}/${TAG_NAME} ${FINAL_LOCATION}/current

sudo systemctl daemon-reload
sudo service les-projets-cagnottes-core start

exit 0
