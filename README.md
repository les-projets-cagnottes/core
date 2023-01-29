# Les Projets Cagnottes - core
[![Release](https://github.com/les-projets-cagnottes/core/workflows/Release/badge.svg)](https://github.com/les-projets-cagnottes/core/actions?query=workflow%3ARelease)
[![Integration](https://github.com/les-projets-cagnottes/core/workflows/Integration/badge.svg)](https://github.com/les-projets-cagnottes/core/actions?query=workflow%3AIntegration)

## Prerequisites

- Java 18 - [Download here](https://jdk.java.net/18/)
- PostgreSQL 12 - [Download here](https://www.postgresql.org/download/)
- A PostgreSQL Client - [Download DBeaver here](https://dbeaver.io/download/)
- Maven 3 - [Download here](https://maven.apache.org/download.cgi)

## Getting Started

1. Create a PostgreSQL database matching the configuration in `src/main/resources/application.properties`
2. Create required environment variables for overriding default config in `src/main/resources/application.properties`
3. Run the following command :
```bash
mvn clean install spring-boot:run
```

## Dynamic set of data for local debug

Once the app is running, you can execute `src/test/resources/sql/dataset.sql` to have test data.
