# user-grpc-service
This is an example project for how to implement [gRPC](https://grpc.io/docs/languages/java/) protocol with spring-boot and [gRPC java](https://grpc.io/docs/languages/java/)

# Getting Started

### Project Setup

* **The interface project** [user-grpc-lib] : Contains the raw protobuf files and generates the java model and service classes.
* **The server project** [user-grpc-server] : Contains the actual implementation of the project and uses the interface project as dependency.
* **The client project** [user-grpc-client] : Contains the HTTP/Grpc client of the project that use the pre-generated stubs to access the server.


### Framework used

* all project modules are build using java 11 and build with gradle
* user-grpc-lib
  * io.grpc libs for proto
  * protoc-gen-grpc-java for generating java classes from proto files using protobuf task
* user-grpc-server
  * user-grpc-lib contains java classes Impl that generated from proto file
  * grpc-server-spring-boot-starter used to integrate grpc server with spring annotations and managing server life cycle
  * spring-boot-starter a lightweight container to manage beans lifecycle and autoconfiguration
  * spring-boot-starter-actuator used for health check of services
  * H2 DB
  * spring-boot-starter-data-jpa
  * spring-kafka used for event driven to publish events on kafka topics
* user-grpc-client
  * user-grpc-lib contains java classes Stubs that generated from proto file
  * grpc-client-spring-boot-starter used to manage grpc client service and autoconfiguration
  * spring-boot-starter
  * spring-boot-starter-web for exposing restful webservices


### Build
From the root folder, just execute the following command which will build/run unit and integration tests
`./gradlew clean build`



### Pre-Run

Please install the following before run the application

#### [gRPCurl](https://github.com/fullstorydev/grpcurl)
Command line tool to interact with gRPC servers
`brew install grpcurl`


#### [Apache Kafka](https://kafka.apache.org/)
Event streaming server which is used to handle user entity update events
`brew install kafka`



### Run

* Start zookeeper server : 
`/usr/local/bin/zookeeper-server-start /usr/local/etc/zookeeper/zoo.cfg`

* Start Kafka instance :
`/usr/local/bin/kafka-server-start /usr/local/etc/kafka/server.properties`

* Start user-grpc-server :
`./gradlew :user-grpc-server:bootRun`

* Start user-grpc-client :
`./gradlew :user-grpc-client:bootRun`


### user-grpc-service APIs

#### Add User
* gRPC :
  `grpcurl -plaintext -d @ localhost:9898 service.UserService/addUser <<EOM
  {
      "first_name": "Alice",
      "last_name": "Bob",
      "nickname": "Ab123",
      "password": "supersecurepassword",
      "email": "alice@bob.com",
      "country": "UK"
  }
EOM`
* REST :
  `curl -X 'POST' \
           'http://localhost:9090/v1/api/users' \
           -H 'Content-Type: application/json' \
           -d '{
                  "first_name": "Alice",
                  "last_name": "Bob",
                  "nickname": "Ab123",
                  "password": "supersecurepassword",
                  "email": "alice@bob.com",
                  "country": "UK"
           }'`


#### Update User
* gRPC :
  `grpcurl -plaintext -d '{ "id":"96ff7869-3c29-4c48-8e44-eb367415c485", "first_name": "Charlie", "country": "PL" }' localhost:9898 service.UserService/updateUser`

* REST :
  `curl -X 'PUT' \
  'http://localhost:9090/v1/api/users/96ff7869-3c29-4c48-8e44-eb367415c485' \
  -H 'Content-Type: application/json' \
  -d '{
          "first_name": "Charlie",
          "country": "PL"
      }'`

#### Delete User
* gRPC :
  `grpcurl -plaintext -d '{ "id":"96ff7869-3c29-4c48-8e44-eb367415c485" }' localhost:9898 service.UserService/deleteUser`

* REST :
  `curl -X 'DELETE' \
  'http://localhost:9090/v1/api/users/96ff7869-3c29-4c48-8e44-eb367415c485'`



#### Search Users
* gRPC :
  `grpcurl -plaintext -d '{ "query":"country=UK" }' localhost:9898 service.UserService/listUsers`

* REST :
  `curl -X GET "http://localhost:9090/v1/api/users?country=UK"`



### Useful commands
* List gRPC services
  `grpcurl -plaintext localhost:9898 list`
* Check service health check
  `grpcurl --plaintext localhost:9898 grpc.health.v1.Health/Check`


### Notes
* The application assumed that nickname should be unique

### Improvement
* Run the application on docker images
* Write more unit and integration tests