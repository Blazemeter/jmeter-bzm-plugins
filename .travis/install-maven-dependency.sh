#/bin/bash
set -ex

if !(mvn dependency:get -Dartifact=$2:$3:$4); then
  mvn com.googlecode.maven-download-plugin:download-maven-plugin:1.4.0:wget -Ddownload.url=$1
  mvn install:install-file -Dfile=target/${1##*/} -DgroupId=$2 -DartifactId=$3 -Dversion=$4 -Dpackaging=jar
fi
