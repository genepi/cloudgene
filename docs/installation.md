# Installation

This section describes how to install the cgapps commandline utility on your Linux or MacOS X workstation.

## Requirements

cgapps is running on Linux and Mac OS X. Windows is currently not supported.

In order to install cgapps, you'll need Java and Docker installed on your system. You can check this with the following commands:

```bash
$ java -version
$ docker -version
```
We tested cgapps with Java 1.7 or higher and Docker 1.6 or higher.


!!! note "Why Docker?":
    Cloudgene works on top of a Hadoop cluster to execute MapReduce workflows. In order to facilitate developing and testing applications, we are using Docker images to start a Hadoop cluster on your local workstation. If you plan to use cgapps to install applications on a Hadoop cluster with an already existing Cloudgene instance, then you don't need Docker.


## Download and Install

If your system fulfills all requirements, then you can download and build the cgapps commandline utility.

Download and unzip the source repository, or clone it using Git:

```bash
git clone git@github.com:lukfor/cloudgene-apps.git
```

Next, we use Maven to compile and build the commandline utility.

```bash
mvn package
```

Maven creates an executable file named `cgapps` in the folder `target/cgapps-0.0.1-bin`. You can add this folder to your `$PATH` variable in order to simplify the execution of the program. For example,

```bash
export PATH=$PATH:/home/lukas/.../target/cgapps-0.0.1-bin
```

Before you start, please enter `gapps version` to validate your installation.

!!! note "Mac OS X,":
     If you are working on Mac OS X, you have to open the __Docker Quickstart Terminal__ and enter all cgapps commands in this terminal window.

You should see something similar like this:

```bash
$ cgapps version

Version: 0.1.0
Docker: localhost
Ping: OK
```

If you see ugly error messages, please check if you have installed Docker correctly.

Congratulation! You are ready to start with cgapps and Cloudgene!
