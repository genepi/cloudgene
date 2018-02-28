# Getting started

You will need the following things properly installed on your computer.

* [Java 8 or higher](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Hadoop](http://hadoop.apache.org/) (Optional)
* [Docker](https://www.docker.com/) (Optional)
* MySQL Server (Optional)


## Installation

You can install Cloudgene via our install script:

```sh
mkdir cloudgene
cd cloudgene
curl -s install.cloudgene.io | bash
```

Test the installation with the following command:

```sh
./cloudgene version
```

We provide a [Docker image](https://github.com/genepi/cloudgene-docker) to get a full-working Cloudgene instance in minutes without any installation.

## Start server

The webservice displays a graphical userinterface for all installed applications. The webserver can be started with the following command:

```sh
cloudgene server
```

The webservice is available on [http://localhost:8082](http://localhost:8082). Please use username `admin` and password `admin1978` to login. You can use the `--port` flag to change the port from `8082` to `8085`:

```sh
cloudgene server --port 8085
```
