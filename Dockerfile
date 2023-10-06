FROM golang:1.21-alpine

WORKDIR /app/

COPY go.mod go.mod
COPY go.sum go.sum
RUN go mod download

COPY cmd cmd
COPY pkg pkg
COPY infra infra
COPY schema schema

RUN go build -o deployer cmd/deployer/main.go
RUN go build -o gateway cmd/gateway/main.go

