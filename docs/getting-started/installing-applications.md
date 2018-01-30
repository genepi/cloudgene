# Installing applications

An online repository of all available applications can be found [here]().

## Install an application

You can install applications by using the `install` command. Cloudgene installs all applications in the folder `apps`.

```sh
cloudgene install <id> <location>
```
The `id` parameter is a shorthand name of the application and is used to refer the application. Location could be a zip file or a yaml file accessible via http or which is located on the local filesystem.

A simple hello world workflow can be installed by using the following command:

```sh
cloudgene install hello-cloudgene http://www.cloudgene.io/downloads/hello-cloudgene
```


## List installed applications

A list of all installed applications can be shown with the `ls` command:


```sh
cloudgene ls
```

This list prints the name and the version of an application and shows you if an application has no syntax errors.


## Remove applications

An installed application can be removed with the `remove` command:


```sh
cloudgene remove <name>
```


## What's next?

Please have a look at the developer documentation or visit our application repository to explore all available Cloudgene applications.
