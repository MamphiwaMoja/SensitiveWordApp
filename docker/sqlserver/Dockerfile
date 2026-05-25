FROM mcr.microsoft.com/mssql/server:2022-latest

USER root

COPY db /docker-entrypoint-initdb.d
COPY docker/sqlserver/healthcheck.sh /usr/local/bin/healthcheck.sh
COPY docker/sqlserver/init-db.sh /usr/local/bin/init-db.sh

RUN chmod +x /usr/local/bin/healthcheck.sh /usr/local/bin/init-db.sh

USER mssql

