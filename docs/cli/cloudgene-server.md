# `cloudgene server`

Starts the Cloudgene web-application which provides an user-interface to submit jobs for all installed workflows.

## Command

```bash
cloudgene server [--host <hadoop_cluster_ip>] [--user <hadoop_username>] [--docker] [--image <docker_image>] [--port]
```
## Parameters

| Parameter                 | Required | Description |
| --- | --- | --- |
| `--host <hadoop_cluster_ip>` | no | Execute all Hadoop steps on this Hadoop JobTracker (default: **use localhost as JobTracker**). |
| `--user <hadoop_username>` | no | Execute Hadoop steps on behalf of this username (default: **cloudgene**) |
| `--conf <HADOOP_CONF>` | no | Path to Hadoop configuration folder (e.g. /etc/hadoop/conf)) |
| `--port` | no | Start the web-application on this port (default: **8082**)

## Examples

### Start server without Hadoop support

```bash
cloudgene server
```


### Start server on the Namenode of a Hadoop Cluster

 Cloudgene uses the default configuration of your Hadoop Cluster. If it is installed on the Hadoop Namenode you can run it without additional flags:

```bash
cloudgene server
```

### Start server and connect it with a remote Hadoop Cluster

```bash
cloudgene server --host <remote-ip>
```

Cloudgene executes your MapReduce steps on the remote cluster. You can use the `--user` flag to set the username which should be used to execute your Hadoop jobs (e.g. it uses the HDFS directory of this user for all files).
