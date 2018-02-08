Cloudgene
=========

[![Build Status](https://travis-ci.org/genepi/cloudgene.svg?branch=master)](https://travis-ci.org/genepi/cloudgene)
[![GitHub release](https://img.shields.io/github/release/genepi/cloudgene.svg)](https://GitHub.com/genepi/cloudgene/releases/)


A framework to build Software As A Service (SaaS) platforms for data analysis pipelines.

- :wrench: **Build** your analysis pipeline in your favorite language or use Hadoop based technologies (MapReduce, Spark, Pig)
- :page_facing_up: **Integrate** your analysis pipeline into Cloudgene by writing a simple [configuration file](http://docs.cloudgene.io/developers/introduction/)
- :bulb: **Get** a powerful web application with user management, data transfer, error handling and more
- :star: **Deploy** your application with one click to any Hadoop cluster or to public Clouds like Amazon AWS
- :cloud: **Provide** your application as SaaS to other scientists and handle thousands of jobs like a pro
- :earth_americas: **Share** your application and enable everyone to clone your service to its own hardware or private cloud instance

## Prerequisites

You will need the following things properly installed on your computer.

* [Java 8 or higher](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Hadoop](http://hadoop.apache.org/) (Optional)
* [Docker](https://www.docker.com/) (Optional)
* MySQL Server (Optional)


## Installation

* `mkdir cloudgene`
* `cd cloudgene`
* `curl -s install.cloudgene.io | bash`

Test the installation with the following command:

```sh
./cloudgene version
```


## Install an application

You can install applications by using the `install` command.

```sh
./cloudgene install <name> <location>
```
Location could be a zip file accessible via http or a yaml file on the local filesystem.

A simple hello world workflow can be installed by using the following command:

```sh
./cloudgene install hello-cloudgene http://www.cloudgene.io/downloads/hello-cloudgene
```


## List installed applications

```sh
./cloudgene ls
```

## Remove applications

```sh
./cloudgene remove <name>
```

## Running a pipeline

### Web-Interface

The webservice displays a graphical userinterface for all installed applications. The webserver can be started with the following command:

```sh
./cloudgene server
```
The webservice is available on http://localhost:8082. Please use username `admin` and password `admin1978` to login.

*For production you should use the [`cloudgene-daemon`](http://docs.cloudgene.io/daemon/introduction/) script.*


### On the commandline

Navigate to the folder where your cloudgene.yaml file is located and start your workflow with the following command:

```sh
./cloudgene run cloudgene.yaml <workflow parameters>
```
or if you installed an application you can start it by entering the name. For example:

```sh
./cloudgene run hello-cloudgene
```

## Running a pipeline with Hadoop steps

Cloudgene needs a Hadoop cluster to execute MapReduce steps. If Cloudgene is installed on the Hadoop Namenode you have to use the `--conf` flag and provide the `$HADOOP_CONF` folder. For example:

```sh
./cloudgene run cloudgene.yaml <workflow parameters> --conf /etc/hadoop/conf
```


## Running Hadoop steps on a remote Hadoop cluster

Navigate to the folder where your cloudgene.yaml file is located and execute your workflow with the `--host` flag to set the ip address of your remote Hadoop cluster:

```sh
./cloudgene run cloudgene.yaml <workflow parameters> --host <remote-ip>
```

Cloudgene executes your MapReduce steps on the remote cluster. You can use the `--user` flag to set the username which should be used to execute your job (e.g. it uses the HDFS directory of this user for all files):

```sh
./cloudgene run cloudgene.yaml <workflow parameters> --host <remote-ip> --user <remote-user>
```


## Development

### How to build the Webinterface


- install nodejs and grunt (see google)

- ``cd src/main/html/webapp``

- ``sudo npm install``

- ``mkdir tmp``

- ``grunt``

- webapp is in dist

- maven builds all jar files and bundles it with webapp
