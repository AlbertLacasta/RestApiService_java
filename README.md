# RestApiService (Java)

## Overview
**Developed in December 2020**

Dependencies:
* Spring Boot
* Maven
* springdoc-openapi

## Why Spring Boot

Spring is a very popular Java-based framework for building web and enterprise applications. Unlike many other frameworks, which focus on only one area, Spring framework provides a wide verity of features addressing the modern business needs via its portfolio project. The main goal of the Spring Boot framework is to reduce overall development time and increase efficiency by having a default setup for unit and integration tests.

In relation to Spring,
Spring Boot aims to make it easy to create Spring-powered, production-grade applications and services with minimum fuss. It takes an opinionated view of the Spring platform so that new and existing users can quickly get to the bits they need.

The primary goals of Spring Boot are:

- To provide a radically faster and widely accessible ‘getting started’ experience for all Spring development.

- To be opinionated out of the box, but get out of the way quickly as requirements start to diverge from the defaults.

- To provide a range of non-functional features that are common to large classes of projects (e.g. embedded servers, security, metrics, health checks, externalized configuration).

**Spring Boot does not generate code and there is absolutely no requirement for XML configuration.**

## Working databases
This Rest Api service allows to work with the following databases:
* postgreSQL

## API documentation
This engine allows to generate a full documentation from tour endpoints.

### WADL
The **Web Application Description Language (WADL)** is a machine-readable **XML** description of HTTP-based web services.
WADL models the resources provided by a service and the relationships between them.
WADL is intended to simplify the reuse of web services that are based on the existing HTTP architecture of the Web.
It is platform and language independent and aims to promote reuse of applications beyond the basic use in a web browser.

```
http://localhost:8080/api/v2/application.wadl
```

### Api Docs
Api docs helps to automate the generation of API documentation using spring boot projects. Springdoc-openapi works by examining an application at runtime to infer API semantics based on spring configurations, class structure and various annotations.

Automatically generates documentation in JSON/YAML and HTML format APIs. This documentation can be completed by comments using swagger-api annotation

Example:
``` Java
   @Operation(summary = "Hello World api endpoint example", description = "This api called hello returns an id from the token that receive")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation"),
    })
    @GetMapping("/hello")
    public ResponseEntity<String> getHello(@RequestHeader("Authorization") String auth) {
        Claims decodedToken = __decodeJWT(auth);
        System.out.println(decodedToken.get("userId"));
        return new ResponseEntity<>(auth, HttpStatus.OK);
    }
```

For run the documentation you have to make a **GET** request to this URL:
```
http://localhost:8080/v3/api-docs
```

## Build and run

### Prerequisites

- Java 8
- Maven 3.0+

### Using the terminal

Go on the project's root folder, then type:

    mvn spring-boot:run

### From Eclipse (Spring Tool Suite)

Import as *Existing Maven Project* and run it as *Spring Boot App*.

### Usage
- Launch the application and go on http://localhost:8080/
