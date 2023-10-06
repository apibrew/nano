echo 'pulling'

git pull

## building faas
mvn package
docker build -f local.Dockerfile . -t apibrew/faas:latest --platform linux/amd64
cd ..
## end building backend

docker push apibrew/faas:latest

