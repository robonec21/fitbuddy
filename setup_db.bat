@echo off
setlocal EnableDelayedExpansion

:: Load environment variables
for /f "tokens=*" %%a in (.env) do (
  set %%a
)

:: Create temporary SQL file with substituted variables
(
echo DROP DATABASE IF EXISTS !DB_NAME!;
echo.
echo -- Connect to PostgreSQL as superuser (postgres^)
echo \connect postgres postgres;
echo.
echo -- Create the database if it doesn't exist
echo CREATE DATABASE !DB_NAME!;
echo.
echo -- Create the user if it doesn't exist
echo CREATE USER !DB_USER! WITH PASSWORD '!DB_PASSWORD!';
echo.
echo -- Connect to the workout database
echo \connect !DB_NAME! postgres;
echo.
echo -- Grant necessary permissions to user
echo GRANT ALL PRIVILEGES ON DATABASE !DB_NAME! TO !DB_USER!;
echo.
echo -- Grant schema permissions
echo GRANT ALL ON SCHEMA public TO !DB_USER!;
echo.
echo -- Make user the owner of the public schema
echo ALTER SCHEMA public OWNER TO !DB_USER!;
echo.
echo -- Grant permissions on future tables (important for Flyway^)
echo ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO !DB_USER!;
echo ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO !DB_USER!;
echo ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO !DB_USER!;
echo ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TYPES TO !DB_USER!;
) > temp_setup.sql

:: Run the SQL file
psql -U postgres -f temp_setup.sql

:: Clean up
del temp_setup.sql

endlocal