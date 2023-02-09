#!/bin/bash
THIS_DIR=$(dirname $(readlink -f $0))
CONF=$THIS_DIR/conf.yml
CONTAINER_NAME=prom_jtfm

if [ -e $CONF ] 
then
  echo "conf found"
else
  echo "conf not found"
  exit 1
fi

docker inspect $CONTAINER_NAME > /dev/null 2>&1
CONTAINER_EXISTS=$?

if [ "$CONTAINER_EXISTS" == "0" ]
then
  echo "container exists"
  CMD="docker start $CONTAINER_NAME"
else
  echo "container not exists"
  CMD="docker run --rm -d --network host --name $CONTAINER_NAME -p 9090:9090 -v $CONF:/etc/prometheus/prometheus.yml prom/prometheus"
fi

$CMD
