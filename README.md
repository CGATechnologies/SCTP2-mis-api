# SCTP MIS API

#### Requirements

- **Runtime Environment**: [Java 16](https://adoptopenjdk.net/?variant=openjdk16&jvmVariant=hotspot)
- **Database**: [MySQL 8](https://dev.mysql.com/downloads/mysql/)

## Development

- **Language**: Java
- **Framework**: Spring Boot
- **API Documentation**: [OpenDoc](https://springdoc.org/) + Swagger UI
- **Build Tool**: [Maven >= 3.8.1](https://maven.apache.org/download.cgi)
- **Database Versioning**: [Flyway Db Migration](https://flywaydb.org/). Migrations are handled inline by the 
    application itself. Place migration files 
    (SQL) in [migration](src/main/resources/db/migration) directory.

#### Flyway database migration file naming strategy

Flyway `out of order` option is on by default. Use the following naming convention: 
`Vmajor.minor.patch.timestampMilliseconds__Description_of_migration.sql`. The unix timestamp value can be obtained from running 
the following in *jshell* console: ``System.currentTimeMillis()`` 
or by using this [HTML file](tools/dbMigrationTimestampGenerator.html). 

Using timestamps prevents potential merge conflicts.

When creating views, tables, functions and procedures, make sure to check for their existence first.
The idea is to make migrations idempotent and avoid migration conflicts. 

#### Building

To quickly build the application, type ``mvn clean package``. A Jar file will be created under the ``target/`` directory.
To skip running tests during build, use ``mvn -DskipTests clean package``.

## Deployment

#### Requirements

- **Runtime Environment**: [Java 16](https://adoptopenjdk.net/?variant=openjdk16&jvmVariant=hotspot)
- **Database**: [MySQL 8](https://dev.mysql.com/downloads/mysql/)

#### Configuration
   
Spring supports different configuration methods. The most widely used method is to create a configuration file 
that corresponds to the target profile of deployment. 

Before running, create a copy of [application-prod.example.yaml] to [application-prod.yaml] and configure the 
    application accordingly.

#### Running

The command to run the application: ``java -jar mis-api-x.x.x.jar``. The application will look for a file named
  `application-prod.properties` or `application-prod.yaml` for the production profile.
  
To run the application using any other profile (dev, test, etc), you can specify the profile using the 
    following command: `java -jar mis-api-x.x.x.jar -Dspring.profiles.active=dev`. Again, this assumes that you have
    a file name `application-dev.yaml` in the same directory as the application.

It is also possible to use a custom profile name, i.e `java -jar mis-api-x.x.x.jar -Dspring.profiles.active=foo`, as 
long as there's a corresponding configuration file.

Again, this is one of the many ways of configuring a Spring Boot application, which allows the build, configuration, 
    and deployment to be automated (DevOps).
    
To use externalized properties (i.e environment variables), you can do this in your configuration file:

```yaml
property:
    foo: some-literal1
    key3: "another literal2"
    bar: ${SOME_ENV_VAR}
```
    
For more Spring Boot configuration reference, see: 

- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/2.4.5/reference/html/index.html)
- [Common Application properties](https://docs.spring.io/spring-boot/docs/2.4.5/reference/html/appendix-application-properties.html#common-application-properties)

#### API Documentation

We are using the Open API Standard. Spring boot exposes the following URLs:

- `${basepath}/api-doc`

  Outputs the machine-readable API documentation in JSON format. The output can be pasted and viewed/edited in 
  [Swagger Editor](https://editor.swagger.io/) or shared.

- `${basepath}/api-doc.yaml`

   Same as the first one but in YAML format.

- `${basepath}/swagger-ui.html`

   Human-readable comprehensive view of the entire API documentation.

> NOTE 

The above URLs are not accessible by default. To access the URLs, set the following properties to true in your 
configuration file.
 It's recommended to only enable them in development mode.

```yaml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

# License

```text
Copyright (C) 2021 CGA Technologies, a trading name of Charlie Goldsmith Associates Ltd
All rights reserved, released under the BSD-3 licence.

CGA Technologies develop and use this software as part of its work but the software 
itself is open-source software; you can redistribute it and/or modify it under the 
terms of the BSD licence below:

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

For more information please see http://opensource.org/licenses/BSD-3-Clause
```