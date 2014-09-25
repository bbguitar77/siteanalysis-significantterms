#!/usr/bin/env bash

# assemble fat JAR (skip-tests)
# mvn clean package -DskipTests=true

JAVA_HEAP_MAX=-Xmx1g
CLASSPATH=${CLASSPATH}:target/siteanalysis-es-standalone.jar

# execute script
#java $JAVA_HEAP_MAX -classpath "$CLASSPATH" com.bwarner.siteanalysis.app.SignificantTermsAggregator "$@"
java $JAVA_HEAP_MAX $JAVA_OPTIONS -jar target/siteanalysis-es-standalone.jar server config/siteanalysis-dw.yml

