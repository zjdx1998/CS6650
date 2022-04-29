# !/bin/bash
docker rmi consumer:latest zjdx1998/consumer:mar11-amd64-latest
sudo docker build -t consumer --platform linux/amd64 .
docker tag consumer zjdx1998/consumer:mar11-amd64-latest
docker push zjdx1998/consumer:mar11-amd64-latest