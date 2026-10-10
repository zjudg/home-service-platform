#!/bin/bash

JAVA_ARGS="-XX:+UnlockExperimentalVMOptions -XX:+UseZGC -XX:ConcGCThread=3 -Xloggc:log/gc.log";

limit_in_bytes=$(cat /sys/fs/cgroup/memory/memory.limit_in_bytes)

RESERVED_MEGABYTES=64

# If not default limit_in_bytes in cgroup

echo $limit_in_bytes

if [ "$limit_in_bytes" -ne "9223372036854771712" ]
then
    limit_in_megabytes=$(expr $limit_in_bytes \/ 1048576)

    echo $limit_in_megabytes

    heap_size=$(expr $limit_in_megabytes - $RESERVED_MEGABYTES)
    
    export JAVA_ARGS="-Xmx${heap_size}M -Xms${heap_size}M $JAVA_ARGS"
fi

export JAVA_ARGS="-server $JAVA_ARGS"

echo $JAVA_ARGS

java --add-opens java.base/java.lang=ALL-UNNAMED -jar app.jar $JAVA_ARGS