#!/bin/bash
export pathToDependencyDir=$1;
export packageManager=$2;
export gradleInitFile=$3;
dependencyFolder=${pathToDependencyDir%/*};
case $packageManager in
  'NPM')
    cd $dependencyFolder;
    npm install license-checker;
    npm install;
    license-checker --json > $dependencyFolder/dependencies.json;
    ;;
  'MAVEN')
    cd $dependencyFolder;
    mvn org.codehaus.mojo:license-maven-plugin:download-licenses;
    ;;
  'GRADLE')
    cd $dependencyFolder;
    gradle generateLicenseReport -I $gradleInitFile;
    ;;
esac