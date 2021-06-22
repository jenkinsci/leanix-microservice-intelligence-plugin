package io.jenkins.plugins.leanixmi.scriptresources;

public class BuildScripts {

    public static final String gradleInitScript = "initscript {\n" +
            "    repositories {\n" +
            "        maven {\n" +
            "            url 'https://plugins.gradle.org/m2/'\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    dependencies {\n" +
            "        classpath 'com.github.jk1:gradle-license-report:1.16'\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "import com.github.jk1.license.render.*\n" +
            "\n" +
            "allprojects {\n" +
            "    rootProject.allprojects {\n" +
            "        apply plugin: 'java'\n" +
            "        apply plugin: com.github.jk1.license.LicenseReportPlugin\n" +
            "        licenseReport {\n" +
            "            // Set custom report renderer, implementing ReportRenderer.\n" +
            "            // Yes, you can write your own to support any format necessary.\n" +
            "            renderers = [new JsonReportRenderer('licenses.json')]\n" +
            "        }\n" +
            "    }\n" +
            "}";
}
