package io.jenkins.plugins.leanixmi;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;

@Extension
public class LeanIXMIGlobalConfiguration extends GlobalConfiguration {

    private String lixhost;
    private String jobresultchoice;


    public LeanIXMIGlobalConfiguration() {
        load();
    }

    public String getLixhost() {
        return lixhost;
    }

    public void setLixhost(String lixhost) {
        this.lixhost = lixhost;
        save();
    }

    public String getJobresultchoice() {
        return jobresultchoice;
    }

    public void setJobresultchoice(String jobresultchoice) {
        this.jobresultchoice = jobresultchoice;
        save();
    }
}
