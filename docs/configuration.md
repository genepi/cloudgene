
# Configuration

This page helps you to configure Cloudgene and describes all parameters of the `settings.yaml`file.


## Database connection

Cloudgene uses default a embedded H2 database to store meta data about jobs and users:

```yaml
database:
  driver: h2
  database: data/cloudgene
  user: cloudgene
  password: cloudgene
```

**For production we recommend to use a MySQL database:**

```yaml
database:
  driver: mysql
  host: localhost
  port: 3306
  user: cloudgene
  password: cloudgene
```

## Mail Server

If no mail server is set, new registered users are activated immediately and no confirmation links are sent. This can be activated by defining a local or remote SMTP mail server:

```yaml
mail:
  smtp: localhost
  port: 25
  user: username
  password: password
  name: noreply@domain.com
```


## HTTPS Certificate and Security

Activate secure Cookies and use your own SSL Certificate to secure your connection:

```yaml
# use https with the provided key store
https: true
httpsKeystore: /your/key.jks
httpsPassword: password
# use secure cookies
secureCookie: true
# use this secret key to generate JWT tokens.
# please use a secret random string
secretKey: some-random-string
```

More about on how to setup a java Keystore can be found [here](http://seppinho.github.io/restlet/webservice/2015/08/31/restlet/).

**TODO: check properties**

## Directories and Workspace

If your service produces a lot of data, it could be useful to set the workspace directories to an other disc. The following directories can be changed:

```yaml
# location for temporary files (e.g. cached file uploads)
tempPath: tmp
# location for the results of a job
localWorkspace: /mnt/new-disc/workspace
# HDFS location for the results of a job
hdfsWorkspace: cloudgene/data
# if set all HDFS files are deleted after job execution
removeHdfsWorkspace: true
# each user can run max 2 jobs at the same time
hdfsAppWorkspace: cloudgene/apps
```

## Queue

Cloudgene manages two different queues to perform setup steps and execution steps for a job. The number of jobs which are executed in parallel can be set for each queue independently:

```yaml
# 5 jobs can execute their setup stpes in parallel
threadsSetupQueue: 5
# 5 jobs can execute their execution stpes in parallel
threadsQueue: 5
# each user can run max 2 jobs at the same time
maxRunningJobsPerUser: 2
```

### Auto-Retire

To change the default values please adapt the following parameters in your `settings.yaml` file:

```yaml
# retire jobs after 6 days
retireAfter: 6
# sent notification after 4 days
notificationAfter: 4
# perform retire as a cronjob once a day.
autoRetire: true
```

!!! Important
    If `autoRetire` is set to `false`, you have to click on the **Retire** button in Administrator Dashboard to clean up.


## Undocumented propeties

```yaml
uploadLimit
urlPrefix
```

```yaml
cluster:
  type: hadoop
  host: localhost
  user: cloudgene
```
