docker container stop $(docker container ls -aq)
docker container rm $(docker container ls -aq)
docker run --name redis-a -d -p 6379:6379 redis:3.0.5
docker run --name redis-b -d -p 6380:6379 redis:3.0.5
docker run --name redis-c -d -p 6381:6379 redis:3.0.5
docker run --name redis-d -d -p 6382:6379 redis:3.0.5