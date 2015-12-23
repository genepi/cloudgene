# application.yaml

The `application.yaml` file defines the content and the properties of your application and exists at the root of your project directory.

An example of a minimal working configuration looks like this:

```
name: lukfor/wordcount
description: MapReduce wordcount example as Cloudgene application
maintainer: lukas forer <lukas.forer@i-med.ac.at>
version: 0.0.1
url: https://github.com/lukfor/cloudgene-examples/tree/master/wordcount
requires: Hadoop MRv1
contents: target/wordcount-0.0.1-assembly
workflows: cloudgene.yaml
```

This file has three functions:

  1. It defines the properties of your application (e.g. name, verstion, dependeces, ...).
  2. It defines the files or folders which should be part of the package
  3. It defines which workflows (i.e. which cloudgene.yaml files) should be installed and accessible via the web-interface.



## Properties

### name
The name of the application as defined in the cloudgene-apps platform

### description
A short description of your application to help users to find it.

### maintainer
Your name and your email address

### version
A unqiue version of your application. Push commands are rejected when the version is not higher than the last shared.

### url
A url pointing to the website or the repository of your application.


### requires
Dependencies are defined as comma-separated list. Based on this property, cgapps decides which docker container is needed. At the moment, the following environments are supported:

  - Hadoop MRv1
  - Hadoop MRV2
  - Spark

If no dependency is defined, cgapps starts a container where only Cloudgene is installed. This setup can be used for all non-Hadoop workflows.

### contents
The folder which contains all files necessary to start your application. This can be either a folder with binaries and scripts or a folder created by build software like Maven.

If no folder is specified, includes all files and folders located in the same directory as the `application.yaml` file.

### workflows
  A list of cloudgene.yaml files which should be installed. If your application provides more workflows, then they can be defined as a comma-separated list. The filenames are relativ paths to the folder defined in the `contents` property.
