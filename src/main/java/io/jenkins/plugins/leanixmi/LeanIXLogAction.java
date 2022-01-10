package io.jenkins.plugins.leanixmi;

import hudson.model.Run;
import jenkins.model.Jenkins;
import jenkins.model.RunAction2;

public class LeanIXLogAction implements RunAction2 {

    private String lxManifestPath;
    private transient Run run;
    private String result;
    private String stage;
    private String version;
    private String dependencymanager;
    private String mavenSettingsPath;
    public static final String TOKEN_FAILED = "Could not send the LeanIX-data, because the Authentication failed. Please check your Hostname and API-Token.";
    public static final String CONFIGFILENOTFOUND = "The configuration file for the LeanIX Plugin could not be found.";
    public static final String SCM_FAILED = "Could not send the LeanIX-data, because the SCM could not be accessed correctly.";
    public static final String MANIFEST_WRONG = "The manifest file could not be parsed, please check that it is correct.";
    public static final String MANIFEST_NOTFOUND = "The manifest file could not be found in your Source Code Management System, please check that the path you specified is correct.";
    public static final String API_CALL_FAILED = "Could not send the LeanIX-data successfully. The API responded with an error. Please check your host name.";
    public static final String API_CALL_EXCEPTION = "Could not send the LeanIX-data to the API successfully. An exception occurred.";
    public static final String SUCCESS = "Success: The LeanIX-data was transmitted successfully.";
    public static final String STAGE_NOTSET = "Deployment stage variable value is not set in jenkins environment.";
    public static final String VERSION_NOTSET = "Deployment version variable or deployment version value is not set in jenkins environment.";
    public static final String DEPENDENCIES_NOT_GENERATED = "The dependencies of your project couldn't be generated.";
    public static final String DEPENDENCY_MANAGER_NOT_SET = "The dependencies of your project couldn't be generated. The dependency manager doesn't seem to be set correctly. Please check your settings.";
    public static final String MAVEN_SETTINGS_FILE_WRONG = "The maven settings file could not be parsed, please check that it is correct.";
    public static final String MAVEN_SETTINGS_FILE_NOTFOUND =
        "The maven settings file could not be found in your Source Code Management "
            + "System, please check that the path you specified is correct.";


    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public String getIconFileName() {
        return Jenkins.RESOURCE_PATH + "/plugin/leanix-microservice-intelligence/images/logo_leanix.png";
    }

    @Override
    public String getDisplayName() {
        return "LeanIX Value Stream Management Log";
    }

    @Override
    public String getUrlName() {
        return "leanixmilog";
    }

    public Run getRun() {
        return run;
    }

    public LeanIXLogAction(String lxManifestPath) {
        this.lxManifestPath = lxManifestPath;
    }

    public String getLxManifestPath() {
        return lxManifestPath;
    }

    public void setLxManifestPath(String lxManifestPath) {
        this.lxManifestPath = lxManifestPath;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDependencymanager() {
        return dependencymanager;
    }

    public void setDependencymanager(String dependencymanager) {
        this.dependencymanager = dependencymanager;
    }

    public String getMavenSettingsPath() {
        return mavenSettingsPath;
    }

    public void setMavenSettingsPath(String mavenSettingsPath) {
        this.mavenSettingsPath = mavenSettingsPath;
    }
}
