FROM maven:3.6.3-jdk-8-slim AS build
#FROM maven:3.3-jdk-8 AS build
run apt-get update && apt-get install -y apt-transport-https
run apt-get install -y python3
run ln -s $(which python3) /usr/bin/python
run apt-get install -y curl
run apt-get install -y  vim 
#run curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
run apt-get install -y python3-pip
run pip3 install numpy
run pip3 install rdflib
run pip3 install pyyaml

WORKDIR /app

EXPOSE 9000

