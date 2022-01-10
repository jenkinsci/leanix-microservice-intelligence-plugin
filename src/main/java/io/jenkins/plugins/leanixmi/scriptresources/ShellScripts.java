package io.jenkins.plugins.leanixmi.scriptresources;

public class ShellScripts {

    public static final String batchScriptWin = "set pathToDependencyDir=%1\n" +
            "set packageManager=%2\n" +
            "set gradleInitFile=%3%\n" +
            "if %packageManager%==NPM (\n" +
            "cd %pathToDependencyDir%\n" +
            "npm install -g license-checker\n" +
            "npm install\n" +
            "license-checker --json > %pathToDependencyDir%/dependencies.json\n" +
            ")\n" +
            "if %packageManager%==MAVEN (\n" +
            "cd %pathToDependencyDir%\n" +
            "mvn org.codehaus.mojo:license-maven-plugin:download-licenses\n" +
            ")\n" +
            "if %packageManager%==GRADLE (\n" +
            "cd %pathToDependencyDir%\n" +
            "gradle generateLicenseReport -I %gradleInitFile%\n" +
            ")";

    public static final String shellScript = "#!/bin/bash\n"
        + "export pathToDependencyDir=$1;\n"
        + "export packageManager=$2;\n"
        + "export gradleInitFileOrMavenSettingsFile=$3;\n"
        + "\n"
        + "case $packageManager in\n"
        + "  'NPM')\n"
        + "    cd $pathToDependencyDir;\n"
        + "    npm install -g license-checker;\n"
        + "    npm install;\n"
        + "    license-checker --json > $pathToDependencyDir/dependencies.json;\n"
        + "    ;;\n"
        + "  'MAVEN')\n"
        + "    cd $pathToDependencyDir;\n"
        + "    \n"
        + "    if [[ $gradleInitFileOrMavenSettingsFile != \"\" ]]; then\n"
        + "      echo \"Maven repository detected with custom user settings (using path $gradleInitFileOrMavenSettingsFile). Attempting to generate dependency file\"\n"
        + "      mvn -s $gradleInitFileOrMavenSettingsFile org.codehaus.mojo:license-maven-plugin:download-licenses;\n"
        + "    else\n"
        + "      echo \"Maven repository detected. Attempting to generate dependency file\"\n"
        + "      mvn org.codehaus.mojo:license-maven-plugin:download-licenses;\n"
        + "    ;;\n"
        + "  'GRADLE')\n"
        + "    cd $pathToDependencyDir;\n"
        + "    gradle generateLicenseReport -I $gradleInitFile;\n"
        + "    ;;\n"
        + "esac";
}
