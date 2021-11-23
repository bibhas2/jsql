#!/bin/sh -f

#Set the folder where the JDBC driver JARs are located
DRIVER_DIR=../apache-tomee-webprofile-8.0.1/lib

java -cp $DRIVER_DIR/mysql-connector-java-8.0.19.jar:$DRIVER_DIR/db2jcc4-10.1.jar:./jsql.jar:. com.webage.JSQL $*