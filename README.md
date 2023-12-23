## CV Demo Backing Backend


Build and run:
```
BUILD=122
mvn clean package
docker build -t us.gcr.io/playground-243019/cv-demo:$BUILD -f Dockerfile .
docker run -it -p 127.0.0.1:8081:8080 us.gcr.io/playground-243019/cv-demo:$BUILD
```

Push:
```
docker push us.gcr.io/playground-243019/cv-demo:$BUILD
```




