
# Enable Hadoop Support

When you execute workflows with Hadoop steps, Cloudgene needs to know where the Namenode and Jobtracker are. These information are in the Hadoop config files in `$HADOOP_HOME/conf`. If you start Cloudgene on the Namenode then this files are already in Cloudgene's classpath.

## Configure Remote Cluster

When you plan to run the Cloudgene on a different server than the Namenode, then you have to define the hostname of Namode/Jobtracker in your `config/settings.yaml` file:

```yaml
cluster:
  # the cluster type. Currently, the only cluster supported is hadoop.
  type: hadoop
  # IP address of Namenode/Jobtracker
  host: localhost
  # username which should be used for job execution
  user: cloudgene
```

## Validate Configuration

Open the **Admin Panel** and click on the **Server** tab. You will see a summary of the Hadoop cluster where you can check if Hadoop is `running` as well as all nodes were found:


![Hadoop](/daemon/images/hadoop.png)
