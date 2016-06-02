# Builds a docker image for the Confluent schema registry.
# Expects links to "kafka" and "zookeeper" containers.
#
# Usage:
#   docker build -t confluent/schema-registry schema-registry
#   docker run -d --name schema-registry --link zookeeper:zookeeper --link kafka:kafka \
#       --env SCHEMA_REGISTRY_AVRO_COMPATIBILITY_LEVEL=none confluent/schema-registry

FROM confluent/platform

MAINTAINER ftrossbach

COPY control-center.properties /usr/local/bin/

#TODO Schema Registry needs a log directory.
RUN ["chown", "-R", "confluent:confluent", "/usr/local/bin/control-center.properties"]


EXPOSE 9021

USER confluent
ENTRYPOINT ["/usr/bin/control-center-start", "/usr/local/bin/control-center.properties"]