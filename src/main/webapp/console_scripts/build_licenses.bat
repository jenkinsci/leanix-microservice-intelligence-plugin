
set pathToDependencyDir=%1
set packageManager=%2
set gradleInitFile=%3%
if %packageManager%==NPM (
cd %pathToDependencyDir%
npm install license-checker
npm install
license-checker --json > %pathToDependencyDir%/dependencies.json
)
if %packageManager%==MAVEN (
cd %pathToDependencyDir%
mvn org.codehaus.mojo:license-maven-plugin:download-licenses
)
if %packageManager%==GRADLE (
cd %pathToDependencyDir%
gradle generateLicenseReport -I %gradleInitFile%
)