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

echo "Waiting for SQL Server at ${DB_HOST}:1433..."
until "$SQLCMD" -S "${DB_HOST}" -U sa -P "${SA_PASSWORD}" -C -Q "SELECT 1" > /dev/null 2>&1; do
  sleep 2
done

# Run the existing assessment scripts in the same order documented for manual setup.
for script in \
  /docker-entrypoint-initdb.d/01_create_database.sql \
  /docker-entrypoint-initdb.d/02_create_schema.sql \
  /docker-entrypoint-initdb.d/03_create_tables.sql \
  /docker-entrypoint-initdb.d/04_seed_data.sql \
  /docker-entrypoint-initdb.d/05_test_connection_and_queries.sql \
  /docker-entrypoint-initdb.d/06_create_app_login_optional.sql
do
  echo "Running ${script}..."
  "$SQLCMD" -S "${DB_HOST}" -U sa -P "${SA_PASSWORD}" -C -i "${script}"
done

echo "Database initialization complete."
