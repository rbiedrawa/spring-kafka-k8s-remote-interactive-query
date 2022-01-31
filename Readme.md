# Spring kafka k8s remote interactive queries (demo)

This repository demonstrates how to implement Kafka remote interactive queries.

## Getting Started

### Prerequisites

* Java 11
* Docker
* Apache Kafka

### Usage

* Build Spring Boot application.
  ```shell
  ./gradlew build
  ```
  
* Build docker image.
  ```shell 
  docker build -t rbiedrawa/spring-kafka-k8s-remote-interactive-query:0.0.1 .
  ```

* Deploy applications to kubernetes.
  ```shell
  # kafka 
  kubectl create -f ./k8s/kafka/
  
  # word-count service
  kubectl create -f ./k8s/word-count/
  ```

* Monitor deployment rollout.
  ```shell
  kubectl rollout status -w deployment/word-count -n word-count
  
  # Waiting for deployment "word-count" rollout to finish: 0 of 3 updated replicas are available...
  # Waiting for deployment "word-count" rollout to finish: 1 of 3 updated replicas are available...
  # Waiting for deployment "word-count" rollout to finish: 2 of 3 updated replicas are available...
  # deployment "word-count" successfully rolled out
  ```

* Port Forward 'word-count' service.
  ```shell
  kubectl port-forward svc/word-count 7777:8080 -n word-count
  ```

* Send dummy messages using either:
    * [Swagger UI](http://localhost:7777/swagger-ui.html)
    * curl
      ```shell
      curl -X POST "http://localhost:7777/words" -H  "accept: */*" -H  "Content-Type: application/json" -d "{\"words\":\"test aaaa bbbb cccc dddd example\"}"
      ```

* Run `kubectl get pods` and note down the names of the word-count pods.
  ```shell
  kubectl get pods -n word-count
  
  #   NAME                        READY   STATUS    RESTARTS   AGE
  # word-count-d85996cb-mjtw6   1/1     Running   0          2m8s
  # word-count-d85996cb-t2bfb   1/1     Running   0          2m8s
  # word-count-d85996cb-v6prg   1/1     Running   0          2m8s
  ```

* For each word-count pod run `kubectl port-forward` command.
  ```shell
  kubectl port-forward <your-pod-name> <local-port>:<your-app-port>
  
  #  kubectl port-forward word-count-d85996cb-mjtw6 8081:8080 -n word-count
  #  kubectl port-forward word-count-d85996cb-t2bfb 8082:8080 -n word-count
  #  kubectl port-forward word-count-d85996cb-v6prg 8083:8080 -n word-count
  ```

* Inspect local state store of each app. Use [Swagger UI](http://localhost:7777/swagger-ui.html) or run below curl
  command.
  ```shell
  curl http://localhost:<local-port>/words
  
  # curl http://localhost:8081/words
  #  [
  #   {
  #     "word": "aaaa",
  #     "count": 2
  #   },
  #   {
  #     "word": "dddd",
  #     "count": 2
  #   },
  #   {
  #     "word": "test",
  #     "count": 2
  #   }
  # ]
  ```

* Try to fetch `word count` from different pod (If word doesn't exist on this pod, data should be obtained from remote
  state store).
  ```shell
   curl http://localhost:8081/words/example
   # {"word":"example","count":2}
  ```

* Check logs to see which host is storing the data.
  ```shell
  kubectl logs <your-pod-name> -n word-count
  
  # kubectl logs word-count-d85996cb-mjtw6 -n word-count
  # 2021-05-26 18:17:44.967 c.r.app.kafka.KafkaWordRepository : Handling key example from different host HostInfo{host='10.1.1.214', port=8080}
  ```

* Delete all k8s resources.
  ```shell
  kubectl delete all --all -n word-count
  kubectl delete ns word-count
  
  kubectl delete all --all -n kafka
  kubectl delete ns kafka
  ```

## Important Endpoints

| Name | Endpoint | 
| -------------:|:--------:|
| `Get Word count` | http://localhost:<APP_PORT>/api/words/{key} |
| `Get all word counts from local state store` | http://localhost:<APP_PORT>/api/words |
| `Swagger UI` | http://localhost:<APP_PORT>/swagger-ui.html |
| `Actuator health` | http://localhost:<APP_PORT>/actuator/health |

## License

Distributed under the MIT License. See `LICENSE` for more information.
