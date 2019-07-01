# Install Applications

An online repository of all available applications can be found [here](http://apps.cloudgene.io).

## Web-Application

Open the **Admin Panel** and click on the **Applications** tab to see all installed applications.

<div class="screenshot">
<img src="../../images/screenshots/install-apps.png">
</div>

After clicking on **Install App** > **from apps.clougene.io** a list of all available applications registed in our [repository](http://apps.cloudgene.io) appears. Choose an application you want to use an click on **Install** to start the installation process.

<div class="screenshot">
<img src="../../images/screenshots/install--app-from-repo.png">
</div>

Depending on your Internet connection and computer resources it could take several minutes. If the installation was successful, you should see your application in the the **Applications** tab:

<div class="screenshot">
<img src="../../images/screenshots/apps.png">
</div>

You can click on **Disable** to deactivate the application or on **Uninstall** to remove it from your Cloudgene instance. Moreover, you can change [permissions](permissions.md) to define which users have access to this application.

## Commandline

### Install an application

You can install also applications by using the [`install`](/cli/cloudgene-install) command. Cloudgene installs all applications in the folder `apps`.

```sh
cloudgene install <location>
```
`Location` could be a zip file or a yaml file accessible via http or which is located on the local filesystem. For example, the following command installs an application that is hosted on a web server:

```sh
cloudgene install https://github.com/lukfor/hello-cloudgene/archive/master.zip
```

You can also install applications directly from GitHub. For example, the latest version of the **hello-cloudgene** application can be installed by using the following command:

```sh
cloudgene install lukfor/hello-cloudgene
```

To install a specific version of a application you can specify a git tag:

```sh
cloudgene install lukfor/hello-cloudgene@1.2.0
```

The ids are created automatically for all applications (e.g. `hello-cloudgene@1.2.0`).


### List installed applications

A list of all installed applications can be shown with the `ls` command:

```sh
cloudgene ls
```

This list prints the name and the version of an application and shows you if an application has no syntax errors.


### Remove applications

An installed application can be removed with the `remove` command:


```sh
cloudgene remove <id>
```

For example to remove **hello-cloudgne 1.2.0** ,

```sh
cloudgene remove hello-cloudgene@1.2.0
```

## What's next?

- [Connect Cloudgene to a Hadoop Cluster](/daemon/hadoop) to enable applications to use MapReduce, Spark, PIG or other Hadoop based technologies
- Install R and RMarkdown to enable applications to create reports
- Install Docker to enable applications to take advantage of Docker images
