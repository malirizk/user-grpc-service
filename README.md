# user-grpc-service
This is example project how to use grpc with spring-boot

# Getting Started

### Project Setup
* **The interface project** : Contains the raw protobuf files and generates the java model and service classes. You probably share this part.
* **The server project** : Contains the actual implementation of your project and uses the interface project as dependency.
* **The client project** : (optional and possibly many) Any client projects that use the pre-generated stubs to access the server.

`./gradlew :grpc-lib:clean :grpc-lib:build`

`./gradlew :grpc-server:bootRun`

### Framework
* [grpc-spring-boot-starter](https://github.com/yidongnan/grpc-spring-boot-starter)
* [gRPC](https://grpc.io/docs/languages/java/)

### [gRPCurl](https://github.com/fullstorydev/grpcurl)
`brew install grpcurl`

* Listing all services
`grpcurl -plaintext localhost:9898 list`

`grpcurl -plaintext localhost:9898 com.usergrpcservice.service.UserService/addUser`

`grpcurl -plaintext -d '{"first_name": "Mohamed", "last_name": "Abdelrazek", "nickname": "Mido", "password": "p@ssword", "email": "mohamed.abdelrazek@gmail.com", "country": "PL"}' \
    localhost:9898 com.usergrpcservice.service.UserService/addUser`

`grpcurl -plaintext -d @ localhost:9898 service.UserService/addUser <<EOM
{
    "first_name": "Mohamed",
    "last_name": "Abdelrazek",
    "nickname": "Mo",
    "password": "p@ssword",
    "email": "mohamed.abdelrazek@gmail.com",
    "country": "PL"
}
EOM`

`grpcurl -plaintext -d @ localhost:9898 service.UserService/listUsers`

`curl -X GET http://localhost:8080/v1/api/users/search`

`curl -X 'POST' \
        'http://localhost:8080/v1/api/users' \
        -H 'Content-Type: application/json' \
        -d '{
                "first_name": "Mohamed",
                "last_name": "Abdelrazek",
                "nickname": "Moo",
                "password": "p@ssword",
                "email": "mohamed.abdelrazek@gmail.com",
                "country": "PL"
            }'`

`grpcurl --plaintext localhost:9898 grpc.health.v1.Health/Check`

`grpcurl --plaintext -d '{"service": "service.UserService"}' localhost:9898 grpc.health.v1.Health/Check`