echo 'pulling'

git pull

## building nano
mvn clean package
docker build -f local.Dockerfile . -t docker-registry.apibrew.io/nano:latest --platform linux/amd64
cd ..
## end building backend

docker push docker-registry.apibrew.io/nano:latest

