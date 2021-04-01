#!/bin/bash
export pathToDependencyDir=$1;
export packageManager=$2;
export gradleInitFile=$3;

case $packageManager in
  'NPM')
    cd $pathToDependencyDir;
    npm install -g license-checker;
    npm install;
    license-checker --json > $pathToDependencyDir/dependencies.json;
    ;;
  'MAVEN')
    cd $pathToDependencyDir;
    mvn org.codehaus.mojo:license-maven-plugin:download-licenses;
    ;;
  'GRADLE')
    cd $pathToDependencyDir;
    gradle generateLicenseReport -I $gradleInitFile;
    ;;
esac