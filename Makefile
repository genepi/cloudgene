.PHONY: build docker

start: docker
	docker run -it -p 8080:80 genepi/cloudgene-dev

docker: build
	docker build -t genepi/cloudgene-dev .

build:
	mvn clean install -DskipTests

default: build
