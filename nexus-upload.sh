#!/bin/bash

# ----------------------------------------
#
# @author: vrakoton
# @version: $Rev$
#
# Description:
#
# This script can be used to upload all your ATG installation classes.jar to 
# a Nexus repository using the maven deploy-file task.
# 
# See https://support.sonatype.com/hc/en-us/articles/213465818-How-can-I-programatically-upload-an-artifact-into-Nexus-
# for reference
#
# ----------------------------------------

# set inital environment variables
NOK=1
OK=0

# ----------------------------------------
# print the script usage
# ----------------------------------------
function usage() {
  cat <<EOF

    usage: $0 --atg-version <atg version> --nexus-url <nexus url> --nexus-repo-id <repository id> --keystore <path to keystore> --keystore-password <pasword>
      --atg-version: the ATG version ex: 11.1.0
      --nexus-url: the nexus url where we wamnt to upload the ATG classes
      --nexus-repo-id: the nexus repository where we want to upload the ATG classes
      --keystore: the JAVA keystore to use for the upload
      --keystore-password: the keystore password (not mandatory). Default is "secret"

EOF
}

# ----------------------------------------
# prints the script execution environment
# ----------------------------------------
function printEnvironment() {
  cat <<EOF
==================================================================================
* JAVA_HOME: ${JAVA_HOME}
* ATG_VERSION: ${ATG_VERSION}
* KEYSTORE_PATH: ${KEYSTORE_PATH}
* DYNAMO_ROOT: ${DYNAMO_ROOT}
* DYNAMO_HOME: ${DYNAMO_HOME}
* NEXUS_REPOSITORY_URL: ${NEXUS_REPOSITORY_URL}
* NEXUS_REPOSITORY_ID: ${NEXUS_REPOSITORY_ID}
==================================================================================
EOF
}


# ----------------------------------------
# Uploads all the classes.jar of your ATG installation
# to your Nexus repository
# ----------------------------------------
function uploadFiles() {
  if [ "x$1" != "x" ]; then
    echo "Uploading files to nexus..."
    for f in ${file_list}; do
      artifact_id=`echo ${f} | tr / .`
      artifact_id=`echo ${artifact_id} | sed -r 's/\.\.||\.lib\.classes\.jar//g'`

      echo "Uploading ${artifact_id}..."
      mvn deploy:deploy-file  \
        -DgroupId=com.oracle.atg \
        -DartifactId=${artifact_id} \
        -Dversion=${ATG_VERSION} \
        -DgeneratePom=true \
        -Dpackaging=jar \
        -DrepositoryId=${NEXUS_REPOSITORY_ID} \
        -Durl=${NEXUS_REPOSITORY_URL} \
        -Dfile=${DYNAMO_ROOT}/${f} \
        -Djavax.net.ssl.trustStore=${KEYSTORE_PATH} \
        -Djavax.net.ssl.trustStorePassword=${KEYSTORE_PASSWORD}
    done
  fi
}

# Check the number of arguments
if [ $# -lt 3 ]; then
  usage
  exit ${NOK}
fi

# parse command line args
while [ "x$1" != "x" ]
do
  case $1 in
    --atg-version)
      ATG_VERSION=$2
      shift
      ;;
    --nexus-url)
      NEXUS_REPOSITORY_URL=$2
      shift
      ;;
    --nexus-repo-id)
      NEXUS_REPOSITORY_ID=$2
      shift
      ;;
    --keystore)
      KEYSTORE_PATH=$2
      shift
      ;;
  esac
  shift
done

if [ "x${KEYSTORE_PASSWORD}" == "x" ]; then
  KEYSTORE_PASSWORD="secret"
fi

# check we have necessary arguments to start the upload
if [ "x${ATG_VERSION}" == "x" ] \
  || [ "x${NEXUS_REPOSITORY_URL}" == "x" ] \
  || [ "x${NEXUS_REPOSITORY_ID}" == "x" ] \
  || [ "x${KEYSTORE_PATH}" == "x" ]; then
  usage
  exit $NOK
fi

echo "Looking for /etc/env/java7..."
[ -f /etc/env/java7 ] && source /etc/env/java7

echo "Looking for /etc/env/atg${ATG_VERSION}..."
[ -f /etc/env/atg${ATG_VERSION} ] && source /etc/env/atg${ATG_VERSION}

# print environment
printEnvironment

# helper: find everything which is not obvious classes.jar
# find . -name '*.jar' | grep -v WEB-INF | grep -v config | grep -v classes.jar | grep -v resources.jar

old_path=`pwd`
cd ${DYNAMO_ROOT}
file_list=`find . -name classes.jar | grep -v '/src/' | grep -v WEB-INF`
uploadFiles ${file_list}
cd ${old_path}



exit $OK