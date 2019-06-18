FROM genepi/cdh5-hadoop-mrv1:latest

MAINTAINER Sebastian Schoenherr <sebastian.schoenherr@i-med.ac.at>, Lukas Forer <lukas.forer@i-med.ac.at>

# Install R
RUN echo "deb http://cran.rstudio.com/bin/linux/ubuntu trusty/" | sudo tee -a /etc/apt/sources.list
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys E084DAB9

RUN sudo apt-get remove maven -y --force-yes
RUN sudo add-apt-repository "deb http://ppa.launchpad.net/natecarlson/maven3/ubuntu precise main"

RUN apt-get update && apt-get install -y --force-yes \
  r-base \
  maven3 \
 && rm -rf /var/lib/apt/lists/*

 ENV M2_HOME=/usr/share/maven3
 ENV M2=$M2_HOME/bin
 ENV PATH=$M2:$PATH

# Install R Packages
RUN R -e "install.packages('knitr', repos = 'http://cran.rstudio.com' )"
RUN R -e "install.packages('markdown', repos = 'http://cran.rstudio.com' )"
RUN R -e "install.packages('rmarkdown', repos = 'http://cran.rstudio.com' )"
RUN R -e "install.packages('ggplot2', repos = 'http://cran.rstudio.com' )"
RUN R -e "install.packages('data.table', repos = 'http://cran.rstudio.com' )"


# Install Cloudgene

RUN mkdir /opt/cloudgene

COPY target/cloudgene-installer.sh /opt/cloudgene/cloudgene-installer.sh
RUN chmod +x /opt/cloudgene/cloudgene-installer.sh
RUN cd /opt/cloudgene; ./cloudgene-installer.sh
RUN chmod +x /opt/cloudgene/cloudgene
ENV PATH=/opt/cloudgene:$PATH

# Add cloudgene.conf to set all dirs to /data
COPY docker/cloudgene.conf /opt/cloudgene/cloudgene.conf

# Add docker specific pages to cloudgene
COPY docker/pages /opt/cloudgene/sample/pages

COPY docker/startup /usr/bin/startup
RUN chmod +x /usr/bin/startup

# Cloudgene Docker Branding
ENV CLOUDGENE_SERVICE_NAME="Cloudgene Docker"
ENV CLOUDGENE_HELP_PAGE="https://github.com/lukfor/docker-cloudgene"
ENV START_CLOUDGENE="true"
ENV START_HADOOP="true"

# Add test workflow to hadoop example directory

# Startup script to start Hadoop and Cloudgene
EXPOSE 80
CMD ["/usr/bin/startup"]
