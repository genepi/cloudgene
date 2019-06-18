.PHONY: build docker

docker: build
	docker build -t genepi/cloudgene .

build:
	mvn clean install

default: build
