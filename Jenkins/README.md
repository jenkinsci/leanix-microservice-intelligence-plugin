
# Caution: This file is work in progress

## Installation prerequisites

Since you are reading this file, you are already accessing the Github project for the MI CICD Jenkins plugin. 
Please pull the entire project into a folder on your machine, if you have not already done so.
Make sure Docker is installed on your computer. Otherwise, please follow the instructions at https://docs.docker.com/get-docker/.
Make sure Java 8 is installed on your computer.

Two ways are described below to run this project in Docker:
With the IDE Intellij and via console. If you want to further develop the plugin, we recommend using IntelliJ.
To better work with IntelliJ install the plugin "Stapler plugin for IntelliJ IDEA" to work with jelly and Stapler.




## IntelliJ

### Project Setup for local development
Use "File" -> "Open" in IntelliJ to open the pom.xml in the project folder and open it as a project. 

Make sure to choose Java8 under "File" -> "Project Structure" -> "Project Settings" -> "Project".


**Setup Maven Run/Debug Configuration via Template: **

Open the "...Edit Configurations" - Dialogue on the upper right corner of the project space and add a configuration by using the "+" button. Choose Maven.  
Choose a name for the configuration, as Working directory choose the project directory. 
In the Command line enter "hpi:run -Djetty.port=8080". <- This lets maven install the dependencies, if it didn't already happen and makes hpi start the jetty server with Jenkins on port 8080 in Debug-Mode.

Once you press the Run or Debug button, the Maven build process should begin.
As soon as the console output shows "INFO: Jenkins is fully up and running" , Jenkins is available at http://localhost:8080/jenkins/ .


### Running and debugging via Docker

#### TODO


## Console

Go to the directory in which you checked out the project from Github and in which the Dockerfile is located. 
Build a docker image by using the following command:

    docker build -t leanix_mi_cicd_connector_plugin_jenkins .

After successfully building, run

    docker run
    -p 8080:8080
    --name leanix_mi_cicd_connector_plugin_jenkins
    leanix_mi_cicd_connector_plugin_jenkins:latest 

After finishing the process of starting the container you will read the following message in the Docker Console:

    "INFO: Jenkins is fully up and running"

You can now use your browser to call
[http://localhost:8080/jenkins/](http://localhost:8080/jenkins/)
where Jenkins is located. 
Try building a new Job in Jenkins and add the build step of the LeanIX CICD Connector.