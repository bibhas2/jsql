# JSQL - Java Command Line SQL Client
JSQL is a command line SQL client that uses JDBC. It is a developer oriented tool. For example, each column in a row is printed in its
own line. 

```
> select CATEGORY_ID, NAME from CATEGORY
```

It will print out:

```
CATEGORY_ID: 10001
NAME: WA1200

CATEGORY_ID: 10002
NAME: WA1300

CATEGORY_ID: 10003
NAME: WA1400
```

This makes it easier to view a large number of columns in a query.

## Building JSQL
Make sure that you have a functioning JDK and Maven installed.

From the command line run:

```
mvn clean package
```

This will produce ``target/jsql.jar``.

## Deploy JSQL
Copy these files to any machine where you wish to run JSQL.

- target/jsql.jar
- jsql.sh
- JSQL.properties

## Configuration
Open ``jsql.sh``. Set the location and names of the JDBC driver JAR files correctly.

Open ``JSQL.properties``. Set the JDBC connection properties for your database. 
The property keys have a configuration name prefix. In the example below, "db2" and "mysql" are configuration names.

```
#DB2 database
db2.driver=com.ibm.db2.jcc.DB2Driver
db2.url=jdbc:db2://localhost:50000/CATALOGDB
db2.user=db2inst1
db2.password=password

#MySQL
mysql.driver=com.mysql.cj.jdbc.Driver
mysql.url=jdbc:mysql://localhost:3306/CATALOGDB
mysql.user=mysqlroot
mysql.password=password
```

## Running JSQL
Launch the tool by giving the configuration name as the only argument. For example:

```
./jsql.sh mysql
```

You can enter any SQL at this point. There are a few special commands.

- quit - end session.
- begin - begin transaction.
- commit - commit transaction.
- rollback - abort transaction.
