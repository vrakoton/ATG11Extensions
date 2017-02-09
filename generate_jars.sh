#!/bin/bash
TARGET_FOLDER=/home/vrakoton/applications/apache-tomcat-7.0.57/lib

[ -f /etc/env/jdk1.7 ] && . /etc/env/jdk1.7 && echo "Java environment initialized"

current_path=`pwd`

cd /home/vrakoton/java-workspace/ATG/ATG11Extensions/DAFAdmin/security/target/classes

echo "Generating classes JAR file ${TARGET_FOLDER}/atgext.jar"
jar -cf ${TARGET_FOLDER}/atgext.jar *


echo "Generating config TAR file /tmp/config.tar.gz"
cd /home/vrakoton/java-workspace/ATG/ATG11Extensions/DAFAdmin/security/config
tar -czf /tmp/groupconfig.tar.gz *

cd ${current_path}
