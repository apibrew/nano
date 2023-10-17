echo 'pulling'

git pull

## building nano
mvn package
docker build -f local.Dockerfile . -t apibrew/nano:latest --platform linux/amd64
cd ..
## end building backend

docker push apibrew/nano:latest

