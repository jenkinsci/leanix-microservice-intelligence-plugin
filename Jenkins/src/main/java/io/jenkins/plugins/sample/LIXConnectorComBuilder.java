package io.jenkins.plugins.sample;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSON;
import org.jenkinsci.Symbol;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.stream.Collectors;


@Extension
public class LIXConnectorComBuilder extends Builder implements SimpleBuildStep, Serializable {

    private String lxmanifestpath;
    private boolean useleanixconnector;

    @DataBoundConstructor
    public LIXConnectorComBuilder() {

    }

    @DataBoundSetter
    public void setUseleanixconnector(boolean useLeanIXConnector) {
        this.useleanixconnector = useLeanIXConnector;
    }

    @NonNull
    public boolean getUseleanixconnector() {
        return useleanixconnector;
    }

    @DataBoundSetter
    public void setLxmanifestpath(String lxManifestPath) {
        this.lxmanifestpath = lxManifestPath;
    }

    @NonNull
    public String getLxmanifestpath() {
        return lxmanifestpath;
    }


    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {

        if (getUseleanixconnector()) {

            String jwtToken = getJWTToken();

            boolean configFound = false;
            LeanIXLogAction logAction = new LeanIXLogAction("Something went wrong. Please review your LeanIx-Configuration!");

            Job job = run.getParent();
            JsonPipelineConfiguration jsonPipelineConfig = new JsonPipelineConfiguration();
            JSONObject jsonConfig = (JSONObject) jsonPipelineConfig.getJsonConfig();
            if (jsonConfig != null) {
                JSONArray lixConfigurations = (JSONArray) jsonConfig.get("leanIXConfigurations");
                for (Object pipeConf : lixConfigurations) {
                    if (pipeConf instanceof JSONObject) {
                        JSONObject pipeConfJson = (JSONObject) pipeConf;
                        JSONArray pipelines = (JSONArray) pipeConfJson.get("pipelines");
                        if (pipelines.contains(job.getName())) {
                            configFound = true;
                            setLxmanifestpath(pipeConfJson.get("path").toString());
                        }
                    }
                }
            }
            if (!configFound) {
                logAction.setLxManifestPath("Path to the manifest wasn't found. Please check your configuration!");
                listener.getLogger().println("Path to the manifest wasn't found. Please check your configuration!");
                setLxmanifestpath("Path to the manifest wasn't found. Please check your configuration!");
            } else {
                listener.getLogger().println("Your manifest path is " + lxmanifestpath + "!");
                logAction.setLxManifestPath(lxmanifestpath);
            }
            run.addAction(logAction);
        }
    }

    private String getJWTToken() {

        // test for the use of API-Token and requesting JWT-Token
        String apiToken = SettingsPanel.getApiToken().getPlainText();
        try {
            URL url = new URL("https://app.leanix.net/services/mtm/v1/oauth2/token");
            String encoding = Base64.getEncoder().encodeToString(("apitoken:" + apiToken).getBytes());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + encoding);
            String postData = "grant_type=client_credentials";
            connection.setRequestProperty("Content-length",
                    String.valueOf(postData.length()));
            connection.setDoOutput(true);
            DataOutputStream output = new DataOutputStream(connection.getOutputStream());
            output.writeBytes(postData);
            output.close();
            InputStream content = connection.getInputStream();
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(content));
            return in.lines().collect(Collectors.joining());
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    @Symbol("leanIXMicroserviceDiscovery")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public static final String defaultLXManifestPath = "/lx-manifest.yml";
        public static final boolean defaultUseLeanIXConnector = true;


        public FormValidation doCheckLxmanifestpath(@QueryParameter String value)
                throws IOException, ServletException {

            if (value.length() == 0)
                return FormValidation.error(Messages.LIXConnectorComBuilder_DescriptorImpl_errors_missingLXManifestPath());
            if (value.length() < 2)
                return FormValidation.warning(Messages.LIXConnectorComBuilder_DescriptorImpl_warnings_tooShort());
            return FormValidation.ok();

        }

        public FormValidation doCheckUseleanixconnector(@QueryParameter boolean useleanixconnector)
                throws IOException, ServletException {
            System.out.println("here");
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.LIXConnectorComBuilder_DescriptorImpl_DisplayLXManifestPath();
        }

    }

}
