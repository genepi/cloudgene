.PHONY: build docker start start-server

start: docker
	docker run -it -p 8080:80 genepi/cloudgene-dev /bin/bash

start-server: docker
	docker run -it -p 8080:80 genepi/cloudgene-dev

docker: build
	docker build -t genepi/cloudgene-dev .

build:
	mvn clean install -DskipTests

default: build
