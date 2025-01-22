#!/bin/bash

# Load environment variables safely
while IFS= read -r line; do
  if [[ ! "$line" =~ ^#.*$ ]] && [[ -n "$line" ]]; then
    eval "export ${line}"
  fi
done < .env

# Create a temporary SQL file with substituted variables
cat > temp_setup.sql << EOF
DROP DATABASE IF EXISTS ${DB_NAME};

-- Connect to PostgreSQL as superuser (postgres)
\connect postgres postgres;

-- Create the database if it doesn't exist
CREATE DATABASE ${DB_NAME};

-- Create the user if it doesn't exist
CREATE USER ${DB_USER} WITH PASSWORD '${DB_PASSWORD}';

-- Connect to the workout database
\connect ${DB_NAME} postgres;

-- Grant necessary permissions to user
GRANT ALL PRIVILEGES ON DATABASE ${DB_NAME} TO ${DB_USER};

-- Grant schema permissions
GRANT ALL ON SCHEMA public TO ${DB_USER};

-- Make user the owner of the public schema
ALTER SCHEMA public OWNER TO ${DB_USER};

-- Grant permissions on future tables (important for Flyway)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ${DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO ${DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO ${DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TYPES TO ${DB_USER};
EOF

# Run the SQL file
psql -U postgres -f temp_setup.sql

# Clean up
rm temp_setup.sql