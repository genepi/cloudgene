
# Getting started

This page helps you to install, configure and running Cloudgene on your own Hadoop MapReduce cluster. Step by step tutorials about the installation of Hadoop can be found on this page.


## Requirements

Cloudgene requires the following software packages in order to work properly.

*   Apache Hadoop (MRv1, CDH4 or CDH5)
*   Java 1.7
*   Optional: R (packages: RMarkdown, knitr, ggplot2)


## Download and Installation

Download the latest version from our download page:

    mkdir cloudgene
    cd cloudgene
    wget http://cloudgene.uibk.ac.at/downloads/cloudgene.latest.zip


Extract the zip file and change the permissions of the executables:

    unzip cloudgene.latest.zip
    chmod +x start.sh state.sh stop.sh


Now you are ready to start and configure Cloudgene.



## Running Cloudgene

Start Cloudgene by entering the following command:

    ./start.sh


Check if Cloudgene is running properly:

    ./state.sh


Cloudgene can be stopped with the following command:

    ./stop.sh




## Configuration

1.  Open your browser and navigate to `http://localhost:8085`.

2.  Login as admin with the default admin password (admin1978)

3.  Change your password immediately (click on Profile or go to `http://localhost:8082/start.html#!pages/profile`)

4.  Go to Admin-Panel and set the path to your Hadoop Installation. If you installed Hadoop using the ClouderaManager the path of your Hadoop Binary is normally `/usr/bin/hadoop`. You can check this by enter `which hadoop` on the commandline and you get the full path.

    ![enter image description here][1]

5.  Next, configure the mail server used by Cloudgene to send notifications and activation links:

    ![enter image description here][2]



## Verifying your Installation

Cloudgene is delivered with a preconfigured application which can be used to test that Cloudgene detects your Hadoop cluster and works properly. To run this test, please follow this steps:

1.  Click on Run and the following formular appears:

    ![enter image description here][3]

2.  Click on "Validate my Configuration" in order to ensure that Cloudgene is able to communicate with your Hadoop Cluster.

3.  If the job fails, please read the error message and adapt your configuration until the job runs successfully.

4.  Now you are ready to use Cloudgene!


## What's next?

Please have a look at the documentation, for a detailed information including the user guide, a list of supported applications and tutorials on how to integrate and deploy your own applications:

*   Install your first Application
*   Developer Guide



 [1]: http://cloudgene.uibk.ac.at/wp-content/uploads/2014/09/hadoop.png
 [2]: http://cloudgene.uibk.ac.at/wp-content/uploads/2014/09/smtp.png
 [3]: http://cloudgene.uibk.ac.at/wp-content/uploads/2014/09/check.png
