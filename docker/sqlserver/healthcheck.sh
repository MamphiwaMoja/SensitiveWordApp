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

"$SQLCMD" -S localhost -U sa -P "${SA_PASSWORD}" -C -Q "SELECT 1" > /dev/null

