# DBService
Microservice to interact with a database(usually PostgreSQL) and map SQL-Tables to a REST-Webservice
## deployment
### requirements
- Docker [Linux Kernel 3.10 or Hyper-V Virtualization under Windows](https://docs.docker.com/install/)
- recommendation: [docker-compose](https://docs.docker.com/compose/install/)
### docker run 

``
docker run -d --rm --name dbservice -e DATABASE_DRIVER=postgresql -e DATABASE_HOST=database -e DATABASE_PORT=5432 -e DATABASE_NAME=dbservice -e DATABASE_PASSWORD=dbservice -e DATABASE_USER=dbservice dbservice 
``
### docker compose

```yaml
version: 3
services:
    dbservice:
        container_name: dbservice
        environment:
            - DATABASE_DRIVER=postgresql
            - DATABASE_HOST=database
            - DATABASE_PORT=5432
            - DATABASE_NAME=dbservice
            - DATABASE_PASSWORD=dbservice
            - DATABASE_USER=dbservice
            - SHOW_SQL=false
        image: dbservice
```
## development
### requirements
- Docker & Docker compose(see deployment)
- Java(OpenJDK > 11)
- Maven 3
### build
build the executable JAR-Files
```bash
mvn clean package
```
building docker container:
```bash
docker build -t dbservice .
```
### environment variables
- DATABASE_DRIVER
    - type of JDBC-Driver
    - current possible types: postgresql(recommended), mysql
    - other databases (e.g. SQLServer, MariaDB, ...) possible because of the hibernate ORM interface
- DATABASE_HOST
    - host for the database connection
- DATABASE_PORT
    - port for the database connection
- DATABASE_NAME=dbservice
    - databasename
- DATABASE_PASSWORD=dbservice
    - username for accesing the database --> need CREATE and ALTER TABLE grants
- DATABASE_USER=dbservice
    - password for the database connection
- SHOW_SQL
    - set the hibernate config "showSql"
    - if set to true(default false) hibernate will log all SQL-Statements to stdout
    - e.g 
    
    ``Hibernate: select hibernatel0_.id as id1_2_, hibernatel0_."group_id" as group_id5_2_, hibernatel0_.label as label2_2_, hibernatel0_.language as language3_2_, hibernatel0_."university_id" as universi6_2_, hibernatel0_.url as url4_2_ from "Link" hibernatel0_ where hibernatel0_.language is null or hibernatel0_.language=''``
    




