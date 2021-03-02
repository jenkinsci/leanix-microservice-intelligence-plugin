package io.jenkins.plugins.sample;

import hudson.model.Run;
import jenkins.model.Jenkins;
import jenkins.model.RunAction2;

public class LeanIXLogAction implements RunAction2 {

    private String lxManifestPath;
    private transient Run run;
    private String result;
    public static final String TOKEN_FAILED = "Could not send the LeanIX-data, because the Authentication failed. Please check your API-Token.";
    public static final String SCM_FAILED = "Could not send the LeanIX-data, because the SCM could not be accessed correctly.";
    public static final String API_CALL_FAILED = "Could not send the LeanIX-data. The SPI responded with an error. Please check your host name and the manifest.yml file in your version management tool.";
    public static final String SUCCESS =  "Success: The LeanIX-data was transmitted successfully.";


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
        return Jenkins.RESOURCE_PATH + "/plugin/leanix_cicd/images/logo_leanix.png";
    }

    @Override
    public String getDisplayName() {
        return "LeanIX-MI-Log";
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

}
