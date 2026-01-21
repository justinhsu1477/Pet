# Use the official MSSQL Server 2019 for Linux image as the base
FROM mcr.microsoft.com/mssql/server:2019-latest

# We need to switch to root user to install packages
USER root

# Install prerequisites and add Microsoft SQL Server tools repository
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

# Switch back to the non-root mssql user
USER mssql
