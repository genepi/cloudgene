
# Configuration

This page helps you to configure Cloudgene and describes all parameters of the `settings.yaml`file.


## Database connection

Cloudgene uses an embedded H2 database to store meta data about jobs and users:

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
  # the email address that Cloudgene uses to send emails
  name: noreply@domain.com
```

## Web-Application

Change the name of your service. If you plan to run Cloudgene on a sub-directory, then you can change the url-prefix.

```yaml
# the name of your service [default: Cloudgene]
name: My Service
# max file size that can be uploaded in Bytes. -1 is unlimited [deafult: -1]
uploadLimit: 50000
# root url of the web-application [default: empty]
urlPrefix: /my-service
```

## HTTPS Certificate and Security

Activate secure Cookies and use your own SSL Certificate to secure your connection:

```yaml
# use https with the provided key store [default: false]
https: true
httpsKeystore: /your/key.jks
httpsPassword: password
# use secure cookies [default: false]
secureCookie: true
# use this secret key to generate JWT tokens.
# please use a secret random string
secretKey: some-random-string
```

More about on how to setup a java Keystore can be found [here](http://seppinho.github.io/restlet/webservice/2015/08/31/restlet/).


## Directories and Workspace

If your service produces a lot of data, it could be useful to set the workspace directories to an other disc. The following directories can be changed:

```yaml
# location for temporary files (e.g. cached file uploads) [default: tmp]
tempPath: tmp
# location for the results of a job [default: workspace]
localWorkspace: /mnt/new-disc/workspace
# HDFS location for the results of a job [default: cloudgene/data]
hdfsWorkspace: cloudgene/data
# if set all HDFS files are deleted after job execution [default: true]
removeHdfsWorkspace: true
# HDFS location for the meta-data of an app [default: cloudgene/apps]
hdfsAppWorkspace: cloudgene/apps
```

## Queue

Cloudgene manages two different queues to execute setup steps and workflow steps for a job. The number of jobs which are executed in parallel can be set for each queue independently:

```yaml
# max. n jobs can execute their setup steps in parallel [default: 5]
threadsSetupQueue: 5
# max. n jobs can execute their workflow steps in parallel  [default: 5]
threadsQueue: 5
# each user can run max. n jobs at the same time  [default: 2]
maxRunningJobsPerUser: 2
```

## Auto-Retire

To change the default values please adapt the following parameters in your `settings.yaml` file:

```yaml
# retire jobs after x days [default: 6]
retireAfter: 6
# sent notification after x days [default: 4]
notificationAfter: 4
# perform retire as a cronjob [default: false].
autoRetire: true
# perform retire cronjobevery x hours [default: 5].
autoRetireInterval: 5

```

!!! Important
    If `autoRetire` is set to `false`, you have to click on the **Retire** button in Administrator Dashboard to clean up.
