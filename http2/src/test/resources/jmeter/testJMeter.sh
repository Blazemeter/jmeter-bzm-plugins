#!/usr/bin/env bash
set -e

export JMETER_VERSION=$1
export JMETER_PATH=$2/$JMETER_VERSION
export JMETER_TEST_PATH=${project.basedir}/target/jmeter-test
export APLN_JAR=$(ls $JMETER_TEST_PATH/lib/ | grep alpn-boot)
export JVM_ARGS="-Xbootclasspath/p:$JMETER_TEST_PATH/lib/alpn-boot.jar"
JARS=$(ls $JMETER_TEST_PATH/lib/ | grep -v alpn-boot)

cd $JMETER_TEST_PATH/lib/
mkdir -p $JMETER_PATH/lib/ext/ && cp -f $JARS $JMETER_PATH/lib/ext/
cd $JMETER_TEST_PATH
bzt -o modules.jmeter.path=$JMETER_PATH -o modules.jmeter.version=$JMETER_VERSION testJMeter.yaml || ERROR=$?
cd $JMETER_PATH/lib/ext/
rm $JARS
exit $ERROR

