package io.jenkins.plugins.sample;


import hudson.Extension;
import hudson.model.RootAction;
import hudson.util.FormApply;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.security.ConfidentialStore;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Entry point to the settings in the Jenkins settings panel.
 *
 * @author Frank Poschner
 */
@Extension
public class SettingsPanel implements RootAction /*, Describable<SettingsPanel>*/ {


    private Secret apitoken;
    private JsonPipelineConfiguration jsonPipelineConfiguration;
    private boolean tokennhostsaved = false;
    private String lixhost = "";
    private boolean lixtokenhostempty = false;

    public SettingsPanel() {
        apitoken = LIXConnectorComBuilder.DescriptorImpl.getApitoken();
    }


    public JsonPipelineConfiguration getJsonPipelineConfiguration() {
        if (jsonPipelineConfiguration == null || jsonPipelineConfiguration.getJsonConfigString() == null) {
            jsonPipelineConfiguration = createNewJsonPipelineConfiguration();
        }
        return jsonPipelineConfiguration;
    }


    public void doSaveAndClearConfig(final StaplerRequest request, final StaplerResponse response) throws Exception {

        if (FormApply.isApply(request)) {
            jsonPipelineConfiguration = createNewJsonPipelineConfiguration();

            FormApply.applyResponse("location.reload();").generateResponse(request, response, null);
        } else {

            JSONObject form = request.getSubmittedForm();
            Object o = form.get("");
            JSONArray a = (JSONArray) o;
            Object formContent = a.get(0);
            jsonPipelineConfiguration.saveConfiguration(formContent.toString());
            response.sendRedirect("");
        }
    }

    public void doSaveApiToken(final StaplerRequest request, final StaplerResponse response) throws Exception {
        setLixtokenhostempty(false);
        setTokennhostsaved(false);
        JSONObject form = request.getSubmittedForm();
        request.bindJSON(this, form);
        Object tokenObject = form.get("apitoken");
        Object lixHostObject = form.get("lixhost");
        if (tokenObject.equals("") || lixHostObject.toString().equals("")) {
            setLixtokenhostempty(true);
        } else {
            LIXConnectorComBuilder.DescriptorImpl.setApitoken(getApitoken());
            setTokennhostsaved(true);
        }
        response.sendRedirect("");
    }

    // we need this method to prettify the JSON - maybe one day only use one library for JSON!
    private JsonPipelineConfiguration createNewJsonPipelineConfiguration() {
        jsonPipelineConfiguration = new JsonPipelineConfiguration();
        JSONObject configObj = JSONObject.fromObject(jsonPipelineConfiguration.getJsonConfigString());
        String configString = configObj.toString(3);
        jsonPipelineConfiguration.setJsonConfigString(configString);
        return jsonPipelineConfiguration;
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

    public Secret getApitoken() {
        return apitoken;
    }

    @DataBoundSetter
    public void setApitoken(Secret apitoken) {
        this.apitoken = apitoken;
    }

    public boolean getTokennhostsaved() {
        return tokennhostsaved;
    }

    public void setTokennhostsaved(boolean tokennhostsaved) {
        this.tokennhostsaved = tokennhostsaved;
    }

    public String getLixhost() {
        return lixhost;
    }

    public void setLixhost(String lixhost) {
        this.lixhost = lixhost;
    }

    public boolean getLixtokenhostempty() {
        return lixtokenhostempty;
    }

    public void setLixtokenhostempty(boolean lixtokenhostempty) {
        this.lixtokenhostempty = lixtokenhostempty;
    }

/*
    @SuppressWarnings("unchecked")
    @Override
    public Descriptor<SettingsPanel> getDescriptor() {
        Jenkins jenkins = Jenkins.get();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins has not been started");
        }
        return jenkins.getDescriptorOrDie(getClass());
    }

    public static DescriptorExtensionList<SettingsPanel, Descriptor<SettingsPanel>> all() {
        return Jenkins.get().getDescriptorList(SettingsPanel.class);
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<SettingsPanel> {

        public FormValidation doCheckJsonConfigTextArea(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.LIXConnectorComBuilder_DescriptorImpl_errors_missingLXManifestPath());
            if (value.length() < 2)
                return FormValidation.warning(Messages.LIXConnectorComBuilder_DescriptorImpl_warnings_tooShort());
            return FormValidation.ok();
        }
    }
*/

    // get all the jobs via Rest API:
    // http://jenkins_url:port/api/json?tree=jobs[name,url]
}

