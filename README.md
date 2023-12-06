# Environment
- Java 17
- Docker, Docker-Compose
- Gradle
- SpringBoot 3.2.0

# MySQL Setting
```bash
cd docker;

docker-compose -f docker-compose-mysql.yaml up -d;

docker exec -it $(docker ps | grep mysql | awk -F" " '{ print $1}') bash;

mysql -u root

create database test;
```
