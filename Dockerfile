# Use the official PostgreSQL image from the Docker Hub
FROM postgres:15-alpine

# Define default environment variables (can be overridden by Docker Compose)
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENV POSTGRES_DB=configuration

# Copy SQL scripts to the Docker container
COPY ./sql /docker-entrypoint-initdb.d/
