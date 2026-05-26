#!/bin/bash
set -euo pipefail

if [ -x /opt/mssql-tools18/bin/sqlcmd ]; then
  SQLCMD=/opt/mssql-tools18/bin/sqlcmd
elif [ -x /opt/mssql-tools/bin/sqlcmd ]; then
  SQLCMD=/opt/mssql-tools/bin/sqlcmd
else
  echo "sqlcmd not found" >&2
  exit 1
fi

DB_HOST="${DB_HOST:-db}"
SA_PASSWORD="${SA_PASSWORD:-${MSSQL_SA_PASSWORD:-}}"

if [ -z "${SA_PASSWORD}" ]; then
  echo "SA_PASSWORD or MSSQL_SA_PASSWORD must be provided" >&2
  exit 1
fi

echo "Waiting for SQL Server at ${DB_HOST}:1433..."

until "$SQLCMD" -S "${DB_HOST}" -U sa -P "${SA_PASSWORD}" -C -Q "SELECT 1" > /dev/null 2>&1; do
  sleep 2
done

for script in \
  /docker-entrypoint-initdb.d/01_create_database.sql \
  /docker-entrypoint-initdb.d/02_create_schema.sql \
  /docker-entrypoint-initdb.d/03_create_tables.sql \
  /docker-entrypoint-initdb.d/04_seed_data.sql \
  /docker-entrypoint-initdb.d/05_test_connection_and_queries.sql \
  /docker-entrypoint-initdb.d/06_create_app_login_optional.sql
do
  echo "Running ${script}..."
  "$SQLCMD" -b -S "${DB_HOST}" -U sa -P "${SA_PASSWORD}" -C -i "${script}"
done

echo "Database initialization complete."
