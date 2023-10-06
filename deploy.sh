echo 'pulling'

git pull

echo 'building deployer'
go build -o deployer cmd/deployer/main.go
echo 'building gateway'
go build -o gateway cmd/gateway/main.go
echo 'building done'

docker build -f local.Dockerfile . -t apibrew/manager:latest --platform linux/amd64


## building backend
cd backend
mvn package
docker build -f local.Dockerfile . -t apibrew/manager-backend:latest --platform linux/amd64
cd ..
## end building backend

docker push apibrew/manager:latest
docker push apibrew/manager-backend:latest

cd infra/helm

helm upgrade manager manager

k0s kubectl rollout restart deployment gateway
k0s kubectl rollout restart deployment deployer
k0s kubectl rollout restart deployment backend

rm -rf deployer
rm -rf gateway
