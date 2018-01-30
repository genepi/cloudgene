# Running apps in the Web Application

The webservice displays a graphical userinterface for all installed applications. The webserver can be started with the following command:

```sh
cloudgene server
```

The webservice is available on http://localhost:8082. Please use username `admin` and password `admin1978` to login. You can use the `--port` flag to change the port from `8082` to `8085`:

```sh
cloudgene server --port 8085
```

*For production you should use the `cloudgene-daemon.sh` script. Learn [more]()*



## Connect it with a Hadoop cluster

Cloudgene needs a Hadoop cluster to execute MapReduce steps. Cloudgene uses the default configuration of your Hadoop Cluster. If it is installed on the Hadoop Namenode you can run it without additional flags. Otherwise you have to provide the url of your Hadoop cluster.

## Running on a remote Hadoop cluster

Start the server with the `--host` flag to set the IP address of your remote Hadoop cluster:

```sh
cloudgene server --host <remote-ip>
```

Cloudgene executes your MapReduce steps on the remote cluster. You can use the `--user` flag to set the username which should be used to execute your job (e.g. it uses the HDFS directory of this user for all files):

```sh
cloudgene server --host <remote-ip> --user <remote-user>
```
