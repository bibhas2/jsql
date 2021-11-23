#!/bin/sh -f

#Set the folder where the JDBC driver JARs are located
DRIVER_DIR=~/Applications/apache-tomee-webprofile-8.0.6/lib

java -cp $DRIVER_DIR/mysql-connector-java-8.0.19.jar:$DRIVER_DIR/db2jcc4-10.1.jar:./jsql.jar:./target/jsql.jar:. com.webage.JSQL $*