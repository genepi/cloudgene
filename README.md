Cloudgene
=========

A framework to build Software As A Service (SaaS) platforms for data analysis pipelines.

[ ![Codeship Status for lukfor/cloudgene](https://app.codeship.com/projects/2e592ca0-ba41-0134-daad-4e53c1da9345/status?branch=master)](https://app.codeship.com/projects/195236) [![codecov](https://codecov.io/gh/lukfor/cloudgene/branch/master/graph/badge.svg?token=b7fe2lDzlV)](https://codecov.io/gh/lukfor/cloudgene)

## Key Features

- **Build** your analysis pipeline in your favorite language or use Hadoop based technologies (MapReduce, Spark, Pig)
- **Integrate** your analysis pipeline into Cloudgene with a simple configuration file
- **Get** a powerful web application within minutes including user management, data transfer, error handling and user notification  
- **Deploy** your application by using our built-in Cloud-adapters to scale up and to benefit from Hadoop
- **Provide** your application as SaaS to other scientists
- **Share** your application and enable scientists to clone your service to their own hardware or cloud instance

## Prerequisites

You will need the following things properly installed on your computer.

* [Java 8 or higher](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Hadoop](http://hadoop.apache.org/) (Optional)
* [Docker](https://www.docker.com/) (Optional)
* MySQL Server (Optional)


## Installation

* `mkdir cloudgene`
* `cd cloudgene`
* `curl -fsSL cloudgene.uibk.ac.at/install | bash`

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
./cloudgene install hello-cloudgene http://cloudgene.uibk.ac.at/downloads/hello-cloudgene
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

*For production you should use the `cloudgene-daemon.sh` script.*


### On the commandline

Navigate to the folder where your cloudgene.yaml file is located and start your workflow with the following command:

```sh
./cloudgene run cloudgene.yaml <workflow parameters>
```
or if you installed an application you can start it by entering the name. For example:

```sh
./cloudgene run hello-cloudgene
```

## How to execute a Cloudgene Pipeline on Hadoop clusters

Cloudgene needs a Hadoop cluster to execute MapReduce steps. Cloudgene uses the default configuration of your Hadoop Cluster. If it is installed on the Hadoop Namenode you can run it without additional flags. Otherwise you have to provide the url of your Hadoop cluster. This same flags work also the `server` command.

### Running on a local workstation using Docker

Navigate to the folder where your cloudgene.yaml file is located and execute your workflow with the `--docker` flag:

```sh
./cloudgene run cloudgene.yaml <workflow parameters> --docker
```

Cloudgene starts automatically the needed docker container and executes your MapReduce steps on the cluster inside the container. We use an [image](https://github.com/seppinho/cdh5-hadoop-mrv1) from seppinho as our default image. You can use the `--image` flag if you want to use a custom docker image (e.g. a fork of our image with some special adaptations you need for your workflow):

```sh
./cloudgene run cloudgene.yaml <workflow parameters> --docker --image myuser/my-image
```


### Running on a remote Hadoop cluster

Navigate to the folder where your cloudgene.yaml file is located and execute your workflow with the `--host` flag to set the ip address of your remote Hadoop cluster:

```sh
./cloudgene run cloudgene.yaml <workflow parameters> --host <remote-ip>
```

Cloudgene executes your MapReduce steps on the remote cluster. You can use the `--user` flag to set the username which should be used to execute your job (e.g. it uses the HDFS directory of this user for all files):

```sh
./cloudgene run cloudgene.yaml <workflow parameters> --host <remote-ip> --user <remote-user>
```

### Reusing Docker containers

Cloudgene's built-in support for docker starts and stops the container each time you execute a workflow. During developement it is more convenient to reuse a running docker container. You have to start the container by hand:

```sh
docker run -it -h cloudgene -p 50030:50030 seppinho/cdh5-hadoop-mrv1:latest run-hadoop-initial.sh
```
When the container is ready, you see the following output:

```
 * Started Hadoop datanode (hadoop-hdfs-datanode): 
 * Started Hadoop namenode: 
 * Started Hadoop secondarynamenode: 
 * Started Hadoop jobtracker: 
 * Started Hadoop tasktracker: 
Congratulations! Cluster is running on 172.17.0.2
```

You can now use the provided address (e.g. 172.17.0.2) to run all Cloudgene workflows in the same container:
```sh
./cloudgene run cloudgene.yaml <workflow parameters> --host 172.17.0.2
```

You can also take advantage of all Hadoop web-interfaces to debug your job (e.g. 172.17.0.2:50030 and 172.17.0.2:50070). 

Don't forget to use `docker ps` and `docker kill` to stop your containers. Please use `docker pull` to ensure you are using the latest image.


## How to build the Webinterface


- install nodejs and grunt (see google)

- ``cd src/main/html/webapp``

- ``sudo npm install``

- ``mkdir tmp``

- ``grunt``

- webapp is in dist

- maven builds all jar files and bundles it with webapp
