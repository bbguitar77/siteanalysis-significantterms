#!/usr/bin/env bash

# assemble fat JAR (skip-tests)
# mvn clean package -DskipTests=true

JAVA_HEAP_MAX=-Xmx1g

# launch container
java $JAVA_HEAP_MAX $JAVA_OPTIONS -jar target/siteanalysis-sigterms-standalone.jar server config/siteanalysis-dw.yml

# execute standalone script (legacy)
# CLASSPATH=${CLASSPATH}:target/siteanalysis-sigterms-standalone.jar
# java $JAVA_HEAP_MAX -classpath "$CLASSPATH" com.bwarner.siteanalysis.app.SignificantTermsAggregator "$@"
