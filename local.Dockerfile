FROM golang:1.21-alpine

WORKDIR /app/

COPY infra infra
COPY /gateway /app/gateway
COPY /deployer /app/deployer

