package io.jenkins.plugins.leanixmi;


import hudson.Extension;
import hudson.model.Result;
import hudson.model.RootAction;
import hudson.util.FormApply;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Entry point to the settings in the Jenkins settings panel.
 *
 * @author Frank Poschner
 */
@Extension
public class SettingsPanel implements RootAction {

    private boolean toggle = false;

    private Secret apitokenpanel;
    private JsonPipelineConfiguration jsonPipelineConfiguration;
    private boolean tokennhostsaved = false;
    private String lixhost = "";
    private boolean lixtokenhostempty = false;
    private String jobresultchoice = Result.SUCCESS.toString();
    private SettingsHandler settingsHandler;


    public SettingsPanel() {
        if (toggle) {
            apitokenpanel = LIXConnectorComBuilder.DescriptorImpl.getApitokenpanel();
        }
        retrieveSettings();
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

    // Only used when toggle for Panel is true.
    public void doSaveApiTokenInPanel(final StaplerRequest request, final StaplerResponse response) throws Exception {
        setLixtokenhostempty(false);
        setTokennhostsaved(false);
        JSONObject form = request.getSubmittedForm();
        request.bindJSON(this, form);
        Object tokenObject = form.get("apitoken");
        Object lixHostObject = form.get("lixhost");
        if (tokenObject.equals("") || lixHostObject.toString().equals("")) {
            setLixtokenhostempty(true);
        } else {
            //this line is used only when toggle for settingspanel is true.
            //LIXConnectorComBuilder.DescriptorImpl.setApitokenpanel(getApitokenpanel());
            settingsHandler.saveSetting("lixhost", lixHostObject.toString());
            setTokennhostsaved(true);
        }
        response.sendRedirect("");
    }

    public void doSaveJobResultChoice(final StaplerRequest request, final StaplerResponse response) throws Exception {

        // ListBoxModel m = new ListBoxModel(new ListBoxModel.Option(Result.SUCCESS.toString()), new ListBoxModel.Option(Result.ABORTED.toString()), new ListBoxModel.Option(Result.FAILURE.toString()), new ListBoxModel.Option(Result.NOT_BUILT.toString()), new ListBoxModel.Option(Result.UNSTABLE.toString()));

        JSONObject form = request.getSubmittedForm();
        this.jobresultchoice = form.getString("jobresultchoice");
        setJobResultChoiceToBuildJob();
        settingsHandler.saveSetting("jobresultchoice", jobresultchoice);
        response.sendRedirect("");
    }

    // we need this method to prettify the JSON - maybe one day only use one library for JSON!
    private JsonPipelineConfiguration createNewJsonPipelineConfiguration() {
        jsonPipelineConfiguration = new JsonPipelineConfiguration();
        jsonPipelineConfiguration.readConfiguration();
        JSONObject configObj = JSONObject.fromObject(jsonPipelineConfiguration.getJsonConfigString());
        String configString = configObj.toString(3);
        jsonPipelineConfiguration.setJsonConfigString(configString);
        return jsonPipelineConfiguration;
    }

    private void retrieveSettings() {
        settingsHandler = new SettingsHandler();
        String tmpResultChoice = (String) settingsHandler.getSettingsObj().get("jobresultchoice");
        if (tmpResultChoice != null && !tmpResultChoice.equals("")) {
            jobresultchoice = tmpResultChoice;
            setJobResultChoiceToBuildJob();
        }
        String tmpLixHost = (String) settingsHandler.getSettingsObj().get("lixhost");
        if (tmpLixHost != null && !tmpLixHost.equals("")) {
            lixhost = tmpLixHost;
        }
    }

    private void setJobResultChoiceToBuildJob() {
        Result res;

        switch (jobresultchoice) {
            case "SUCCESS":
                res = Result.SUCCESS;
                break;
            case "ABORTED":
                res = Result.ABORTED;
                break;
            case "FAILURE":
                res = Result.FAILURE;
                break;
            case "NOT_BUILT":
                res = Result.NOT_BUILT;
                break;
            case "UNSTABLE":
                res = Result.UNSTABLE;
                break;
            default:
                res = Result.SUCCESS;
                break;
        }
        LIXConnectorComBuilder.DescriptorImpl.setJobresultchoicecentral(res);
    }


    public String getJobresultchoice() {
        return jobresultchoice;
    }

    public void setJobresultchoice(String jobresultchoice) {
        this.jobresultchoice = jobresultchoice;
    }

    public String getIconFileName() {
        return Jenkins.RESOURCE_PATH + "/plugin/leanix_cicd/images/logo_leanix.png";
    }

    public String getDisplayName() {
        return "LeanIX Microservice Intelligence";
    }

    public String getUrlName() {
        return "leanix-microservice-intelligence";
    }

    public Secret getApitokenpanel() {
        return apitokenpanel;
    }

    @DataBoundSetter
    public void setApitokenpanel(Secret apitokenpanel) {
        this.apitokenpanel = apitokenpanel;
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

}