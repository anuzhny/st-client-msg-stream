#!/bin/bash

export JAVA_HOME=/opt/jdk1.8.0_31
export PATH=$PATH:$HOME/bin

export CLASSPATH="/opt/cloudera/parcels/CDH-5.4.2-1.cdh5.4.2.p0.2/lib/hadoop/hadoop-common.jar:/opt/cloudera/parcels/CDH-5.4.2-1.cdh5.4.2.p0.2/lib/hadoop/client/commons-logging.jar:/opt/cloudera/parcels/CDH-5.4.2-1.cdh5.4.2.p0.2/lib/hadoop/client/guava.jar:/opt/cloudera/parcels/CDH-5.4.2-1.cdh5.4.2.p0.2/lib/hadoop/client/commons-collections.jar:/opt/cloudera/parcels/CDH-5.4.2-1.cdh5.4.2.p0.2/lib/hadoop/client/protobuf-java.jar:/opt/cloudera/parcels/CDH-5.4.2-1.cdh5.4.2.p0.2/lib/hadoop/client/commons-lang.jar:/opt/cloudera/parcels/CDH-5.4.2-1.cdh5.4.2.p0.2/lib/hadoop/client/commons-configuration.jar:/opt/cloudera/parcels/CDH-5.4.2-1.cdh5.4.2.p0.2/lib/hadoop/client/hadoop-auth.jar:/opt/cloudera/parcels/CDH-5.4.2-1.cdh5.4.2.p0.2/lib/hadoop/client/slf4j-api.jar"

SPARK_CLASS_PATH=$CLASSPATH

spark-submit --driver-class-path /opt/cloudera/parcels/CDH/lib/hbase/lib/htrace-core-3.1.0-incubating.jar --conf  "spark.executor.extraClassPath=/opt/cloudera/parcels/CDH/lib/hbase/lib/htrace-core-3.1.0-incubating.jar" --master yarn --driver-memory 4g --executor-memory 3g --num-executors 6 --deploy-mode cluster --jars /home/stcuscol/clientMsg/lib/st-client-msg-stream-assembly_2.10-1.0.jar --name "ClientMsg Collector" --class "com.mt.spark.streaming.ClientMsg" /home/stcuscol/clientMsg/lib/st-client-msg-stream_2.10-2.0.jar
