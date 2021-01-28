FROM openjdk:8-jdk

# ----
# Install Maven
ARG MAVEN_VERSION=3.6.3
ARG USER_HOME_DIR="/root"
RUN mkdir -p /usr/share/maven && \
curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar -xzC /usr/share/maven --strip-components=1 && \
ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
# speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"


# make source folder and set working directory
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# Create the maven folder manually
RUN mkdir -p /root/.m2 && mkdir /root/.m2/repository

# Copy maven settings
COPY settings.xml /usr/share/maven/ref/
COPY settings.xml /root/.m2/

# install maven dependency packages (keep in image)
COPY pom.xml /usr/src/app

# copy other source files (keep in image)
COPY src /usr/src/app/src
#Debug port for Jetty
EXPOSE 8080
CMD [ "mvn", "hpi:run", "-Djetty.port=8080"]
