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
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;


@Extension
public class LIXConnectorComBuilder extends Builder implements SimpleBuildStep, Serializable {

    private String lxmanifestpath;
    private boolean useleanixconnector;
    private String hostname = "";
    private String apitoken;
    private String jobresultchoice = "";
    private static final String pathNotFoundMsg = "Path to the manifest wasn't found. Please check your configuration!";
    private static final String exceptionMsg = "Please check your LeanIX credentials (hostname and apitoken).";

    @DataBoundConstructor
    public LIXConnectorComBuilder() {

    }

    @DataBoundSetter
    public void setUseleanixconnector(boolean useLeanIXConnector) {
        this.useleanixconnector = useLeanIXConnector;
    }

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

    @DataBoundSetter
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getHostname() {
        return hostname;
    }

    @DataBoundSetter
    public void setApitoken(String apitoken) {
        this.apitoken = apitoken;
    }

    public String getApitoken() {
        return apitoken;
    }


    public String getJobresultchoice() {
        return jobresultchoice;
    }

    @DataBoundSetter
    public void setJobresultchoice(String jobresultchoice) {

        this.jobresultchoice = jobresultchoice;
        if (!jobresultchoice.equals("")) {
            DescriptorImpl.setJobresultchoice(Result.fromString(jobresultchoice));
        }
    }


    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {

        if (getUseleanixconnector()) {


            boolean configFound;
            LeanIXLogAction logAction = new LeanIXLogAction("Something went wrong. Please review your LeanIX-Configuration!");

            Job job = run.getParent();
            configFound = findJSONPipelineConfig(job);
            if (!configFound) {
                logAction.setLxManifestPath(pathNotFoundMsg);
                logAction.setResult(pathNotFoundMsg);
                listener.getLogger().println(pathNotFoundMsg);
                setLxmanifestpath(pathNotFoundMsg);
                run.setResult(LIXConnectorComBuilder.DescriptorImpl.getJobresultchoice());
            } else {
                listener.getLogger().println("Your manifest path is " + lxmanifestpath + "!");
                logAction.setLxManifestPath(lxmanifestpath);
                ManifestFileHandler manifestFileHandler = new ManifestFileHandler();
                boolean manifestFileFound = manifestFileHandler.retrieveManifestJSONFromSCM(lxmanifestpath, job, run, launcher, listener, logAction);

                // If SCM was checked out correctly
                if (run.getResult() != null && manifestFileFound) {
                    String jwtToken = getJWTToken();
                    if (jwtToken != null && !jwtToken.equals("")) {
                        //TODO: Get correct deployment version and stage
                        int responseCode = manifestFileHandler.sendFileToConnector(jwtToken, "1.0.0", "test");
                        if (responseCode < 200 || responseCode > 308) {
                            logAction.setResult(LeanIXLogAction.API_CALL_FAILED);
                        }
                    } else {
                        run.setResult(DescriptorImpl.getJobresultchoice());
                        logAction.setResult(LeanIXLogAction.TOKEN_FAILED);
                    }
                }
            }
            run.addAction(logAction);
        }
    }

    private boolean findJSONPipelineConfig(Job job) {
        boolean configFound = false;
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
        return configFound;
    }

    private String getJWTToken() {
        // test for the use of API-Token and requesting JWT-Token
        String apiToken = this.getApitoken();
        String token;

        try {
            // TODO: Apply the hostname here to URL (from Credentials)
            URL url = new URL("https://app.leanix.net/services/mtm/v1/oauth2/token");
            String encoding = Base64.getEncoder().encodeToString(("apitoken:" + apiToken).getBytes(StandardCharsets.UTF_8));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + encoding);
            String postData = "grant_type=client_credentials";
            connection.setRequestProperty("Content-length",
                    String.valueOf(postData.length()));
            connection.setDoOutput(true);
            DataOutputStream output = new DataOutputStream(connection.getOutputStream());
            output.writeBytes(postData);
            BufferedReader in = null;
            String collection;
            try (InputStream content = connection.getInputStream()) {
                in = new BufferedReader(new InputStreamReader(content, StandardCharsets.UTF_8));
                collection = in.lines().collect(Collectors.joining());
                JSONObject jsonObject = (JSONObject) JSONValue.parse(collection);
                token = (String) jsonObject.get("access_token");
            } finally {
                if (in != null)
                    in.close();
                if (output != null) {
                    output.close();
                }
                connection.disconnect();
            }
            return token;
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exceptionMsg;
    }

    @Symbol("leanIXMicroserviceDiscovery")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public static final String defaultLXManifestPath = "/lx-manifest.yml";
        public static final boolean defaultUseLeanIXConnector = true;
        private static Result jobresultchoice;


        public FormValidation doCheckLxmanifestpath(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.LIXConnectorComBuilder_DescriptorImpl_errors_missingLXManifestPath());
            if (value.length() < 2)
                return FormValidation.warning(Messages.LIXConnectorComBuilder_DescriptorImpl_warnings_tooShort());
            return FormValidation.ok();
        }

        public FormValidation doCheckJobresultchoice(@QueryParameter String value)
                throws IOException, ServletException {
            if (!value.equals("") && !value.equals("FAILURE") && Result.fromString(value).equals(Result.FAILURE)) {
                return FormValidation.error(Messages.LIXConnectorComBuilder_DescriptorImpl_errors_severityLevelWrong());
            }
            return FormValidation.ok();
        }


        //only used when toggle for settingspanel is true.
        private static Secret apitokenpanel;

        public static Secret getApitokenpanel() {
            return apitokenpanel;
        }

        public static void setApitokenpanel(Secret apitokenpanel) {
            DescriptorImpl.apitokenpanel = apitokenpanel;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.LIXConnectorComBuilder_DescriptorImpl_DisplayLXManifestPath();
        }

        public static Result getJobresultchoice() {
            return jobresultchoice;
        }

        public static void setJobresultchoice(Result jobresultchoice) {
            DescriptorImpl.jobresultchoice = jobresultchoice;
        }
    }
}