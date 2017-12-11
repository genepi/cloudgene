# Running apps on the Commandline

Navigate to the folder where your cloudgene.yaml file is located and start your workflow with the following command:

```sh
cloudgene run cloudgene.yaml <workflow parameters>
```
or if you installed an application you can start it by entering the name. For example:

```sh
cloudgene run hello-cloudgene
```

## How to execute a Cloudgene Pipeline on Hadoop clusters

Cloudgene needs a Hadoop cluster to execute MapReduce steps. Cloudgene uses the default configuration of your Hadoop Cluster. If it is installed on the Hadoop Namenode you can run it without additional flags. Otherwise you have to provide the url of your Hadoop cluster. This same flags work also the `server` command.

## Running on a local workstation using Docker

Navigate to the folder where your cloudgene.yaml file is located and execute your workflow with the `--docker` flag:

```sh
cloudgene run cloudgene.yaml <workflow parameters> --docker
```

Cloudgene starts automatically the needed docker container and executes your MapReduce steps on the cluster inside the container. We use an [image](https://github.com/seppinho/cdh5-hadoop-mrv1) from seppinho as our default image. You can use the `--image` flag if you want to use a custom docker image (e.g. a fork of our image with some special adaptations you need for your workflow):

```sh
cloudgene run cloudgene.yaml <workflow parameters> --docker --image myuser/my-image
```


## Running on a remote Hadoop cluster

Navigate to the folder where your cloudgene.yaml file is located and execute your workflow with the `--host` flag to set the ip address of your remote Hadoop cluster:

```sh
./cloudgene run cloudgene.yaml <workflow parameters> --host <remote-ip>
```

Cloudgene executes your MapReduce steps on the remote cluster. You can use the `--user` flag to set the username which should be used to execute your job (e.g. it uses the HDFS directory of this user for all files):

```sh
cloudgene run cloudgene.yaml <workflow parameters> --host <remote-ip> --user <remote-user>
```

## Reusing Docker containers

Cloudgene's built-in support for docker starts and stops the container each time you execute a workflow. During developement it is more convenient to reuse a running docker container. You have to start the container by hand:

```sh
docker run -it -h cloudgene -p 50030:50030 seppinho/cdh5-hadoop-mrv1:latest run-hadoop-initial.sh
```
When the container is ready, you see the following output:

```ansi
 * Started Hadoop datanode (hadoop-hdfs-datanode):
 * Started Hadoop namenode:
 * Started Hadoop secondarynamenode:
 * Started Hadoop jobtracker:
 * Started Hadoop tasktracker:
Congratulations! Cluster is running on 172.17.0.2
```

You can now use the provided address (e.g. 172.17.0.2) to run all Cloudgene workflows in the same container:

```sh
cloudgene run cloudgene.yaml <workflow parameters> --host 172.17.0.2
```

You can also take advantage of all Hadoop web-interfaces to debug your job (e.g. 172.17.0.2:50030 and 172.17.0.2:50070).

Don't forget to use `docker ps` and `docker kill` to stop your containers. Please use `docker pull` to ensure you are using the latest image.
