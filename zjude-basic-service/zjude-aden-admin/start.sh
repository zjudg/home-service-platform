#!/bin/bash

JAVA_ARGS="-XX:+UseContainerSupport -XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=75.0 -Xloggc:log/gc.log";

export JAVA_ARGS="-server $JAVA_ARGS"

echo $JAVA_ARGS

java --add-opens java.base/java.lang=ALL-UNNAMED -Djava.security.egd=file:/dev/urandom -jar app.jar $JAVA_ARGS