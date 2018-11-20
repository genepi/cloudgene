
# Installation

This page helps you to install, configure and running Cloudgene.


## Requirements


You will need the following things properly installed on your computer.

* [Java 8 or higher](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Hadoop](http://hadoop.apache.org/) (Optional) Step by step tutorials about the installation of Hadoop can be found on this [page]().
* [Docker](https://www.docker.com/) (Optional)
* MySQL Server (Optional)


## Download and Installation

Download the latest version from our download page using the following commands:

* `mkdir cloudgene`
* `cd cloudgene`
* `curl -s install.cloudgene.io | bash`

Test the installation with the following command:

```sh
./cloudgene version
```

Now you are ready to [start Cloudgene](/daemon/getting-started) and [install applications](/daemon/install-apps).

## Docker image

We provide a [Docker image](https://github.com/genepi/cloudgene-docker) to get a full-working Cloudgene instance in minutes without any installation. After the successful installation of [Docker](http://www.docker.io), all you need to do is:

```bash
docker run -d -p 8080:80 genepi/cloudgene
```

After about 1 minute you are able to access your Cloudgene instance on [http://localhost:8080](http://localhost:8080). Please use username `admin` and password `admin1978` to login.

You can now [install applications](/daemon/install-apps) and submit jobs.

## Manual installation

All releases are also available on [Github](https://github.com/genepi/cloudgene/releases).
