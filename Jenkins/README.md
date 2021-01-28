# Work in progress

# Installation

Since you are reading this file, you are already accessing the Github project for the MI CICDJenkins plugin. 
Please pull the entire project into a folder on your machine, if you have not already done so.
Make sure Docker is installed on your computer. Otherwise, please follow the instructions at https://docs.docker.com/get-docker/.
Make sure Java 8 is installed on your computer.

Two ways are described below to run this project in Docker:
With the IDE Intellij and via console. If you want to further develop the plugin, we recommend using IntelliJ.



## IntelliJ

### Project Setup for local development
Use "File" -> "Open" in IntelliJ to open the pom.xml in the project folder and open it as a project. 

Make sure to choose Java8 under "File" -> "Project Structure" -> "Project Settings" -> "Project".


**Setup Maven Run/Debug Configuration via Template: **

Open the "...Edit Configurations" - Dialogue on the upper right corner of the project space and add a configuration by using the "+" button. Choose Maven.  
Choose a name for the configuration, as Working directory choose the project directory. 
In the Command line enter "hpi:run -Djetty.port=8090". <- This lets maven install the dependencies, if it didn't already happen and makes hpi start the jetty server with Jenkins on 
port 8080 in Debug-Mode.

Once you press the Run or Debug button, the Maven process should begin, at the end of which Jenkins should be accessible at http://localhost:8080.


### Running Docker

## Console

Go to the directory in which you checked out the project from Github. Build a docker image by using the following command:
docker build -t leanix_mi_cicd_connector_plugin_jenkins .
After successfully building, run

docker run
-p 8080:8080
--name leanix_mi_cicd_connector_plugin_jenkins
leanix_mi_cicd_connector_plugin_jenkins:latest 