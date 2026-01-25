# Use the official MSSQL Server 2019 for Linux image
FROM mcr.microsoft.com/mssql/server:2019-latest

# Switch to root to install tools
USER root

# Install SQL Server tools
RUN apt-get update && \
    apt-get install -y curl apt-transport-https gnupg && \
    mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor -o /etc/apt/keyrings/microsoft.gpg && \
    echo "deb [arch=amd64 signed-by=/etc/apt/keyrings/microsoft.gpg] https://packages.microsoft.com/ubuntu/20.04/prod focal main" > /etc/apt/sources.list.d/mssql-release.list && \
    apt-get update && \
    ACCEPT_EULA=Y apt-get install -y mssql-tools unixodbc-dev && \
    rm -rf /var/lib/apt/lists/*

# Add sqlcmd to PATH
ENV PATH="${PATH}:/opt/mssql-tools/bin"

# Create directory for init scripts
RUN mkdir -p /docker-entrypoint-initdb.d

# Copy initialization scripts
COPY init-db.sql /docker-entrypoint-initdb.d/
COPY entrypoint.sh /usr/local/bin/

# Make entrypoint executable
RUN chmod +x /usr/local/bin/entrypoint.sh

# Switch back to mssql user
USER mssql

# Expose SQL Server port
EXPOSE 1433

# Use custom entrypoint
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
