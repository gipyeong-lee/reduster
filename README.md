# reduster
High Performance Redis Cluster Manager

# Overview

K/V String을 저장할 수 있는 분산 스토리지 라이브러리를 구현한다.

# Spec

Language : kotlin
Redis : 3.0.5

# 기능

- [x] get(k) : String
- [x] set(k : String, v: String)
- [x] mget(keys : Set<String>) : Map<String,String>
- [x] mset(kvs : Map<String,String>)
- [x] scale-out, scale-in

# 요구사항

- [x] 분산 방법은 client-side에서 미리 세팅된 redis node 4개에 consistent hashing을 이용
- [x] 이후에 확장을 통해 다른 스토리지를 이용 할 수 있도록 공통 interface를 구현
- [x] Scale-out, scale-in 고려
- [x] 1-10만개 의 아이템을 입력/읽기를 하는 tc를 작성
- [x] 성능테스트까지 수행 -> 데모로 대체

# Overview

## Jumping Consistent Hash Algorithm

- 참고한 알고리즘 논문 : https://arxiv.org/pdf/1406.2294.pdf

## How to test this project.

#### 1. startup redis container
```
./start_redis.sh
```

또는 docker-compose가 설치되어 있을 경우 아래와 같이 실행도 가능합니다.
```bash
# run docker services as daemon
docker-compose up -d

# down
docker-compose down
```

#### 2. run webapp
```
cd webapp
yarn install && yarn start
``` 

# Known issue
- 노드의 생성,삭제시 읽기, 쓰기 문제 존재
