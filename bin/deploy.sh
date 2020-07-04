#!/bin/bash

TAG_NAME=$1
DEPLOY_LOCATION="$( cd "$(dirname "$0")" || exit ; pwd -P )"/..
FINAL_LOCATION=/opt/les-projets-cagnottes/core
FINAL_APP_LOCATION=/opt/les-projets-cagnottes/core/${TAG_NAME}

echo "$(date -u) Automatic Deploy"  >> ./console.log

sudo service les-projets-cagnottes-core stop
sleep 1

# Backup environment config
cp "${FINAL_LOCATION}/current/setenv.sh" "${DEPLOY_LOCATION}/setenv.sh"
cp "${FINAL_LOCATION}/current/application.properties" "${DEPLOY_LOCATION}/application.properties"

# Update systemd
sudo cp "${DEPLOY_LOCATION}/bin/les-projets-cagnottes-core.service" /etc/systemd/system/les-projets-cagnottes-core.service

# Create folder for version
mkdir -p "${FINAL_APP_LOCATION}"
cp "${DEPLOY_LOCATION}/bin/setenv.sh.template" "${FINAL_APP_LOCATION}/setenv.template"
cp "${DEPLOY_LOCATION}/bin/les-projets-cagnottes-core.sh" "${FINAL_APP_LOCATION}/les-projets-cagnottes-core.sh"
cp "${DEPLOY_LOCATION}/target/core-${TAG_NAME}.jar" "${FINAL_APP_LOCATION}/les-projets-cagnottes-core.jar"
cp "${DEPLOY_LOCATION}/setenv.sh" "${FINAL_APP_LOCATION}/setenv.sh"
cp "${DEPLOY_LOCATION}/application.properties" "${FINAL_APP_LOCATION}/application.properties"

# Update current symlink
cd "${FINAL_LOCATION}" || exit
rm current
ln -s "${TAG_NAME}" current

# Restart service
sudo systemctl daemon-reload
sudo service les-projets-cagnottes-core start

exit 0
