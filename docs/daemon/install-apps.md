# Install Applications

An online repository of all available applications can be found [here](http://apps.cloudgene.io).

## Web-Application

Open the **Admin Panel** and click on the **Applications** tab to see all installed applications.

<div class="screenshot">
<img src="/images/screenshots/install-apps.png">
</div>

After clicking on **Install App** > **from apps.clougene.io** a list of all available applications registed in our [repository](http://apps.cloudgene.io) appears. Choose an application you want to use an click on **Install** to start the installatiojn process.

<div class="screenshot">
<img src="/images/screenshots/install--app-from-repo.png">
</div>

Depending on your Internet connection and computer resources it could take several minutes. If the installation was successful, you should see your application in the the **Applications** tab:

<div class="screenshot">
<img src="/images/screenshots/apps.png">
</div>

You can click on **Disable** to deactivate the application or on **Uninstall** to remove it from your Cloudgene instance.

## Commandline

### Install an application

You can install applications by using the [`install`](/cli/cloudgene-install) or the [`github-install`](/cli/cloudgene-github-install) command. Cloudgene installs all applications in the folder `apps`.

```sh
cloudgene install <id> <location>
```
The `id` parameter is a shorthand name of the application and is used to refer the application. Location could be a zip file or a yaml file accessible via http or which is located on the local filesystem.

```sh
cloudgene install hello-cloudgene https://github.com/lukfor/hello-cloudgene/archive/master.zip
```

You can also install applications directly from GitHub:

```sh
cloudgene github-install <owner>/<repository>
```

For example, the **hello-cloudgene** application can be installed by using the following command:

```sh
cloudgene github-install lukfor/hello-cloudgene
```

The ids are created are automatically for all applications from GitHub.

### List installed applications

A list of all installed applications can be shown with the `ls` command:

```sh
cloudgene ls
```

This list prints the name and the version of an application and shows you if an application has no syntax errors.


### Remove applications

An installed application can be removed with the `remove` command:


```sh
cloudgene remove <name>
```

## What's next?

- [Connect Cloudgene to a Hadoop Cluster](/daemon/hadoop) to enable applications to use MapReduce, Spark, PIG or other Hadoop based technologies
- Install R and RMarkdown to enable applications to create reports
- Install Docker to enable applications to take advantage of Docker images
