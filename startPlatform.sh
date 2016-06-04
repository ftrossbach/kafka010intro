#!/bin/bash

#wget http://packages.confluent.io/archive/3.0/confluent-3.0.0-2.11.zip
#unzip confluent-3.0.0-2.11.zip
#cd confluent-3.0.0

rm -rf /tmp/kafka*
rm -rf /tmp/zookeeper

# Start Zookeeper
./confluent-3.0.0/bin/zookeeper-server-start ./confluent-3.0.0/etc/kafka/zookeeper.properties > zk.log &

# Give Zookeeper some time to start
sleep 5s

# Start a single Kafka Broker
./confluent-3.0.0/bin/kafka-server-start ./confluent-3.0.0/etc/kafka/server.properties > broker.log &
# Give Broker some time to start
sleep 5s

# Start Schema Registry
./confluent-3.0.0/bin/schema-registry-start ./confluent-3.0.0/etc/schema-registry/schema-registry.properties > registry.log &

# Give Schema Registry some time to start
sleep 5s

# Start Kafka Connect
./confluent-3.0.0/bin/connect-distributed connect-distributed.properties > connect.log &

# Give Connect some time to start
sleep 5s

# Start Confluent Control Center (dauert eine Weile)
./confluent-3.0.0/bin/control-center-start control-center.properties > ccc.log &

./confluent-3.0.0/bin/kafka-topics --create --topic faust --zookeeper localhost:2181 --partitions 1 --replication-factor 1
./confluent-3.0.0/bin/kafka-topics --create --topic faust-shout --zookeeper localhost:2181 --partitions 1 --replication-factor 1
./confluent-3.0.0/bin/kafka-topics --create --topic faust-count --zookeeper localhost:2181 --partitions 1 --replication-factor 1

curl -X POST -H "Content-Type: application/json" --data '{"name": "faust-source", "config": {"connector.class":"org.apache.kafka.connect.file.FileStreamSourceConnector", "tasks.max":"1", "topic":"faust", "file":"/Users/ftr/Projects/kafka010intro/faust.txt" }}' http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data '{"name": "faust-sink", "config": {"connector.class":"org.apache.kafka.connect.file.FileStreamSinkConnector", "tasks.max":"1", "topics":"faust", "file":"/Users/ftr/Projects/kafka010intro/faust-sink.txt" }}' http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data '{"name": "faust-shout-sink", "config": {"connector.class":"org.apache.kafka.connect.file.FileStreamSinkConnector", "tasks.max":"1", "topics":"faust-shout", "file":"/Users/ftr/Projects/kafka010intro/faust-shout.txt" }}' http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data '{"name": "faust-count-sink", "config": {"connector.class":"org.apache.kafka.connect.file.FileStreamSinkConnector", "tasks.max":"1", "topics":"faust-count", "file":"/Users/ftr/Projects/kafka010intro/faust-count.txt" }}' http://localhost:8083/connectors
