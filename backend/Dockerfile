FROM mariadb:latest

COPY goshelf_base.sql /docker-entrypoint-initdb.d/goshelf_base.sql

ENV MYSQL_ROOT_PASSWORD=ilovebooks
ENV MYSQL_DATABASE=goshelf
