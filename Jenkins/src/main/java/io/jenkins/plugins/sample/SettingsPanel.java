package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.model.RootAction;
import jenkins.model.ModelObjectWithContextMenu;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.util.List;

/**
 * Entry point to the preferences in the Jenkins preference panel.
 *
 * @author Frank Poschner
 */
@Extension
public class SettingsPanel extends ManagementLink {

    private String textAreaInput = "Input still to come";

    public String getIconFileName() {
        return "gear.png";
    }

    public String getDisplayName() {
        return "LeanIX-Microservice-Discovery";
    }

    public String getUrlName() {
        return "lix-mi-discovery";
    }

    public String getTextAreaInput() {
        return textAreaInput;
    }

    // get all the jobs via Rest API:
    // http://jenkins_url:port/api/json?tree=jobs[name,url]

    @DataBoundSetter
    public void setTextAreaInput(String textAreaInput) {
        this.textAreaInput = textAreaInput;
    }
}