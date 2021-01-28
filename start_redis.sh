docker container stop $(docker container ls -aq)
docker container rm $(docker container ls -aq)

docker run --name redis-a -d -p 6379:6379 redis:3.0.5 redis-server --port 6379
docker run --name redis-b -d -p 6380:6380 redis:3.0.5 redis-server --port 6380
docker run --name redis-c -d -p 6381:6381 redis:3.0.5 redis-server --port 6381
docker run --name redis-d -d -p 6382:6382 redis:3.0.5 redis-server --port 6382
