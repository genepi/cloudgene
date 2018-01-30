# Running apps on the Commandline

Navigate to the folder where your cloudgene.yaml file is located and start your workflow with the following command:

```sh
cloudgene run cloudgene.yaml <workflow parameters>
```
or if you installed an application you can start it by entering the name. For example:

```sh
cloudgene run hello-cloudgene
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
