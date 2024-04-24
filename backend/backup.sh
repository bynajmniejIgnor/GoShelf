#!/bin/bash
container=$(docker ps | grep book_keeper | awk {'print $1'})
docker exec $container mariadb-dump -u root -pilovebooks goshelf > goshelf_backup.sql

