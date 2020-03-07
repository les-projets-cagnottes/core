#!/bin/bash

TAG_NAME=$1
TMP_LOCATION="$( cd "$(dirname "$0")" ; pwd -P )"/..
FINAL_LOCATION=/opt/les-projets-cagnottes/core

echo "$(date -u) Automatic Deploy"  >> ./console.log

sudo service les-projets-cagnottes-core stop
sleep 1

mkdir -p ${FINAL_LOCATION}
sudo cp ${TMP_LOCATION}/les-projets-cagnottes-core.service /etc/systemd/system/les-projets-cagnottes-core.service
cp ${TMP_LOCATION}/setenv.sh.template ${FINAL_LOCATION}/setenv.template
cp ${TMP_LOCATION}/les-projets-cagnottes-core.sh ${FINAL_LOCATION}/les-projets-cagnottes-core.sh
cp ${TMP_LOCATION}/core-${TAG_NAME}.jar ${FINAL_LOCATION}/les-projets-cagnottes-core.jar

sudo systemctl daemon-reload
sudo service les-projets-cagnottes-core start

exit 0
