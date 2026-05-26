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

SA_PASSWORD="${SA_PASSWORD:-${MSSQL_SA_PASSWORD:-}}"

if [ -z "${SA_PASSWORD}" ]; then
  echo "SA_PASSWORD or MSSQL_SA_PASSWORD must be provided" >&2
  exit 1
fi

"$SQLCMD" -S localhost -U sa -P "${SA_PASSWORD}" -C -Q "SELECT 1" > /dev/null