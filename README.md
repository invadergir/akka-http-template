# Template / Starter CRUD Service for Akka-HTTP

## Goals of this project

1. To be a starter project for a basic RESTful CRUD service with get, post, put, and delete endpoints.  Includes configuration (pureconfig) and logging (scala-logging) plumbing already set up.
1. To show how to properly do asynchronous endpoints in akka-http 
1. To show how to properly handle exceptions in the service code.
1. To be a demonstration of how to organize the files (notice that ThingService and ThingRoutes are separate).
1. To show how to do unit tests using akka-http-testkit.

## To Use

### Run

```
sbt test run
```

The server runs on localhost:8080.

To test it, you can query for the default Thing which is available at startup:

```
curl -vX GET http://localhost:8080/things/hello
```

Which gives:
```
{"id":"hello","description":"world!"}
```

Other example usages:

```
# Create 'a' thing.
curl -vX POST -H "Content-Type: application/json" -d '{"id": "a", "description":"aaa"}' http://localhost:8080/things

# Update 'a' thing:
curl -vX PUT -H "Content-Type: application/json" -d '{"id": "a", "description":"AAAAA"}' http://localhost:8080/things/a

# Get that thing:
curl -vX GET http://localhost:8080/things/a

# Get all the things:
curl -vX GET http://localhost:8080/things

# Delete 'a' thing:
curl -vX DELETE http://localhost:8080/things/a
```

### Build Jar

```
sbt assembly
```

### To Rename Project 

To use this as a starter project, you can run the 'renameproject.sh' script as follows:

```
./renameproject.sh new-project-name -p $NEW_PACKAGE_PREFIX
```
...where NEW_PACKAGE_PREFIX is "com.mycompany" or something like that.

For help running the script, please type: 

```
./renameproject.sh -h
```
