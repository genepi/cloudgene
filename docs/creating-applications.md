# Creating applications

This section describes how you can create and deploy your first Cloudgene application using cgapps and the Cloudgene-App platform.

---

## Create an application from scratch

You can either create the `application.yaml` file manually or use the `init` command to build the needed folder layout:

```sh
$ cgapps init my-username/my-application
$ cd my-application
```
cgapps creates a new directory called "my-application" and creates a minimal working `application.yaml` file:

```ansi
my-application
  |── application.yaml
  |── README.md
```

The `application.yaml` file defines the content and the properties of your application. In addition,all application should contain a readme file with some informations about the project it self (learn more about the application.yaml file).

---

## Create an application based on a boilerplate


By using the `--boilerplate` option you can select a boilerplate. A boilerplate is a ready-to-run application which can be used to start your own project. For example, we provide templates to start immediately a new MapReduce project which includes all needed files to build the java code using maven and the ready-to-use `application.yaml` file to package and deploy your application.

The source code of all boilerplates is available at https://github.com/lukfor/cloudgene-boilerplates


### cmd

Boilerplate for workflow with command-line programs.

```bash
$ cgapps init my-username/my-cmd-application --boilerpate cmd
```


### mapreduce

Boilerplate for MapReduce workflow.

```bash
$ cgapps init my-username/my-mapreduce-application --boilerpate mapreduce
```

### cloudflow

Boilerplate for a Cloudflow pipeline.

```bash
$ cgapps init my-username/my-cloudflow-application --boilerpate cloudflow
```

---

## Test your application

cgapps enables us to test our application during the development process. To test your application, navigate to the folder where your application.yaml file is located and execute the test command:

```bash
$ cgapps test
```

cgapps starts automatically the needed docker container and mounts your cloudgene application.

!!! note "Auto-Reloading":
    You can edit files and workflows (or even recompile your java source code using maven)  outside the container and Cloudgene updates automatically the web-interface.

---

## Share your application

When you are ready to deploy and share your application, sign-up for the cloudgene-apps platform and create a new shorthand for your application.
Ensure that the name in the `application.yaml` file is exactly the same as the just created shorthand.

Next, navigate to your application folder and execute the pull command:

```bash
$ cgapps push
```

The `push` command builds a zip archive of your package and uploads it to your cloudgene-apps repository. You have to enter the password of your cloudgene-apps account in order to deploy new versions.

If the deployment process was successful, then other users can install your application with the `pull` command.
