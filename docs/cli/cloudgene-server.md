# `cloudgene server`

Starts the Cloudgene web-application which provides an user-interface to submit jobs for all installed workflows.

## Command

```bash
cloudgene server [--conf <hadoop_conf_dir>] [--user <hadoop_username>] [--port <port>]
```
## Parameters

| Parameter                 | Required | Description |
| --- | --- | --- |
| `--conf <hadoop_conf_dir>` | no | Path to Hadoop configuration folder (e.g. `/etc/hadoop/conf`)) |
| `--user <hadoop_username>` | no | Execute Hadoop steps on behalf of this username (default: **cloudgene**) |
| `--port <port>` | no | Start the web-application on this port (default: **8082**)

## Examples

### Start server

```bash
cloudgene server
```

The webservice is available on http://localhost:8082. Please use username `admin` and password `admin1978` to login. You can use the `--port` flag to change the port from `8082` to `8085`:

```bash
cloudgene server --port 8085
```

### Start server and connect it with a Hadoop Cluster

If Cloudgene is installed on the Hadoop Namenode, then you can start the server with the conf files located in `$HADOOP/conf`:

```bash
cloudgene server --conf /etc/hadoop/conf
```

However, if Cloudgene is not installed on the Hadoop Namenode, then you have to copy the config files from the Hadoop Namenode to your Cloudgene instance and start the server with this configurations:

```bash
cloudgene server --conf  $CLOUDGENE_HOME/hadoop-conf
```

Cloudgene executes your MapReduce steps on the remote cluster. You can use the `--user` flag to set the username which should be used to execute your Hadoop jobs (e.g. it uses the HDFS directory of this user for all files).

More about Hadoop configuration can be found [here](/daemon/hadoop).
