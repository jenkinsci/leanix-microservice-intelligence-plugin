package io.jenkins.plugins.sample;

import hudson.model.Run;
import jenkins.model.Jenkins;
import jenkins.model.RunAction2;

public class LeanIXLogAction implements RunAction2 {

    private String lxManifestPath;
    private transient Run run;


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

}
