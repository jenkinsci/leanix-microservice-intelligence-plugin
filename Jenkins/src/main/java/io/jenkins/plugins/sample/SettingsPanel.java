package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.Util;
import hudson.model.RootAction;
import hudson.util.FormApply;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.util.Enumeration;

/**
 * Entry point to the settings in the Jenkins settings panel.
 *
 * @author Frank Poschner
 */
@Extension
public class SettingsPanel implements RootAction {

    private JsonPipelineConfiguration jsonPipelineConfiguration;

    public JsonPipelineConfiguration getJsonPipelineConfiguration() {
        if (jsonPipelineConfiguration == null|| jsonPipelineConfiguration.getJsonConfig() == null) {
            jsonPipelineConfiguration = new JsonPipelineConfiguration();
        }
        return jsonPipelineConfiguration;
    }


    public void doSaveAndClearConfig(final StaplerRequest request, final StaplerResponse response) throws Exception {


        if (FormApply.isApply(request)) {
            jsonPipelineConfiguration.setJsonConfig("");
            response.sendRedirect("/jenkins/" + getUrlName());
        } else {

            JSONObject form = request.getSubmittedForm();
            jsonPipelineConfiguration.saveConfiguration(form.toString());

            response.sendRedirect(request.getContextPath());
        }
    }

    public FormValidation doCheckJsonConfigTextarea(@QueryParameter String value) {
        if (Util.fixEmptyAndTrim(value) == null) {
            return FormValidation.error("foo cannot be empty");
        }
        return FormValidation.ok();
    }


    public String getIconFileName() {
        return Jenkins.RESOURCE_PATH + "/plugin/leanix_cicd/images/logo_leanix.png";
    }

    public String getDisplayName() {
        return "LeanIX-Microservice-Discovery";
    }

    public String getUrlName() {
        return "lix-mi-discovery";
    }

    // get all the jobs via Rest API:
    // http://jenkins_url:port/api/json?tree=jobs[name,url]


}