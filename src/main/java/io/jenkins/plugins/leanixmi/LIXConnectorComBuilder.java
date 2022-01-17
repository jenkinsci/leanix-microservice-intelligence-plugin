package io.jenkins.plugins.leanixmi;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
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
import org.jenkinsci.Symbol;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;


@Extension
public class LIXConnectorComBuilder extends Builder implements SimpleBuildStep, Serializable {

    private String lxmanifestpath;
    private String dependencymanager = "";
    private boolean useleanixconnector;
    private String hostname = "";
    private Secret apitoken;
    private String jobresultchoice = "";
    private String deploymentstage;
    private String deploymentversion;
    private String mavensettingspath = "";

    @DataBoundConstructor
    public LIXConnectorComBuilder() {
        // the constructor is needed, even if it is empty!
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

    public String getLxmanifestpath() {
        return lxmanifestpath;
    }


    public String getDependencymanager() {
        return dependencymanager;
    }

    @DataBoundSetter
    public void setDependencymanager(String dependencymanager) {
        if (Arrays.asList(DescriptorImpl.DEPENDENCYMANAGERCHOICES).stream().anyMatch(dependencymanager::equalsIgnoreCase)) {
            this.dependencymanager = dependencymanager;
        }
    }

    @DataBoundSetter
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getHostname() {
        return hostname;
    }

    @DataBoundSetter
    public void setApitoken(Secret apitoken) {
        this.apitoken = apitoken;
    }

    public Secret getApitoken() {
        return apitoken;
    }

    public String getJobresultchoice() {
        return jobresultchoice;
    }

    @DataBoundSetter
    public void setJobresultchoice(String jobresultchoice) {
        this.jobresultchoice = jobresultchoice;
    }

    public String getDeploymentstage() {
        return deploymentstage;
    }

    @DataBoundSetter
    public void setDeploymentstage(String deploymentstage) {
        this.deploymentstage = deploymentstage;
    }

    public String getDeploymentversion() {
        return deploymentversion;
    }

    @DataBoundSetter
    public void setDeploymentversion(String deploymentversion) {
        this.deploymentversion = deploymentversion;
    }

    public String getMavensettingspath() {
        return mavensettingspath;
    }

    @DataBoundSetter
    public void setMavensettingspath(String mavensettingspath) {
        this.mavensettingspath = mavensettingspath;
    }

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env, @NonNull Launcher launcher, @NonNull TaskListener listener) throws InterruptedException, IOException {


        if (getUseleanixconnector()) {

            if (jobresultchoice == null || jobresultchoice.equals("")) {
                jobresultchoice = DescriptorImpl.getJobresultchoicecentral().toString();
            }

            boolean configFound;
            LeanIXLogAction logAction = new LeanIXLogAction("Something went wrong. Please review your LeanIX-Configuration!");
            logAction.setDependencymanager(dependencymanager);

            Job job = run.getParent();
            configFound = findJSONPipelineConfig(job);
            if (!configFound) {
                logAction.setResult(LeanIXLogAction.CONFIGFILENOTFOUND);
                listener.getLogger().println(LeanIXLogAction.CONFIGFILENOTFOUND);
                run.setResult(Result.fromString(getJobresultchoice()));
            } else {
                listener.getLogger().println("Your manifest path is " + lxmanifestpath + "!");
                logAction.setLxManifestPath(lxmanifestpath);


                // Dealing with stage and version here
                // Setting default values "stage" and "version"
                String stage = "stage";
                String version = "version";
                // env.get can lead to a NullPointerException in case that the variable is not existing
                try {
                    stage = env.get(deploymentstage);
                    version = env.get(deploymentversion);
                } catch (Exception e) {
                    System.out.println(e);
                }

                if (stage != null && !stage.equals("")) {
                    listener.getLogger().println("Your deployment stage is " + stage + ".");
                    logAction.setStage(stage);
                } else {
                    logAction.setStage(LeanIXLogAction.STAGE_NOTSET);
                    listener.getLogger().println(LeanIXLogAction.STAGE_NOTSET);
                    setDeploymentstage(LeanIXLogAction.STAGE_NOTSET);
                }

                // Version is mandatory, don't go on without it!
                if (version != null && !version.equals("")) {
                    listener.getLogger().println("Your deployment version is " + version + ".");
                    logAction.setVersion(version);


                    ManifestFileHandler manifestFileHandler = new ManifestFileHandler(jobresultchoice);
                    String folderPath = Jenkins.get().getRootDir() + "/leanix/git/" + job.getDisplayName() + "/checkout";
                    File folderPathFile = new File(folderPath);
                    boolean manifestFileFound = manifestFileHandler.retrieveManifestJSONFromSCM(lxmanifestpath, job, run, launcher, listener, logAction, folderPathFile);
                    DependencyHandler dependencyHandler = new DependencyHandler();


                    // If SCM was checked out correctly
                    if (run.getResult() != null && manifestFileFound) {

                        File projectDependencies =
                                dependencyHandler.createProjectDependenciesFile(dependencymanager, folderPathFile, folderPath, listener, logAction,
                                    mavensettingspath);
                        if (projectDependencies == null) {
                            run.setResult(Result.fromString(getJobresultchoice()));
                        }

                        String host = this.getHostname();
                        String apiToken;
                        if (env.get("token") != null) {
                            apiToken = env.get("token");
                        } else {
                            apiToken = this.getApitoken().getPlainText();
                        }
                        String jwtToken = getJWTToken(host, apiToken, logAction, listener);
                        if (jwtToken != null && !jwtToken.isEmpty()) {
                            ConnectorHandler conHandler = new ConnectorHandler();

                            int responseCode = conHandler.sendFilesToConnector(host, jwtToken, version, stage, dependencymanager, projectDependencies, manifestFileHandler.getManifestJSON(), logAction, listener);

                            if (responseCode < 200 || responseCode > 308) {
                                run.setResult(Result.fromString(getJobresultchoice()));
                            }
                        } else {
                            run.setResult(Result.fromString(getJobresultchoice()));
                        }

                    } else {
                        logAction.setLxManifestPath(LeanIXLogAction.MANIFEST_NOTFOUND);
                        logAction.setResult(LeanIXLogAction.MANIFEST_NOTFOUND);
                        listener.getLogger().println(LeanIXLogAction.MANIFEST_NOTFOUND);
                        setLxmanifestpath(LeanIXLogAction.MANIFEST_NOTFOUND);
                    }
                } else {
                    logAction.setVersion(LeanIXLogAction.VERSION_NOTSET);
                    listener.getLogger().println(LeanIXLogAction.VERSION_NOTSET);
                    logAction.setResult("Could not finish successfully: " + LeanIXLogAction.VERSION_NOTSET);
                    setDeploymentversion(LeanIXLogAction.VERSION_NOTSET);
                    run.setResult(Result.fromString(getJobresultchoice()));
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
            JSONObject lxConfigurations = (JSONObject) jsonConfig.get("leanIXConfigurations");
            deploymentstage = (String) lxConfigurations.get("deploymentStageVarName");
            deploymentversion = (String) lxConfigurations.get("deploymentVersionVarName");
            JSONArray pathSettings = (JSONArray) lxConfigurations.get("settings");
            for (Object pipeConf : pathSettings) {
                if (pipeConf instanceof JSONObject) {
                    JSONObject pipeConfJson = (JSONObject) pipeConf;
                    JSONArray pipelines = (JSONArray) pipeConfJson.get("pipelines");
                    if (pipelines.contains(job.getName())) {
                        configFound = true;
                        setLxmanifestpath(pipeConfJson.get("path").toString());
                        setDependencymanager((pipeConfJson.get("dependency-manager") != null) ? pipeConfJson.get("dependency-manager").toString() : "");
                    }
                }
            }
        }
        return configFound;
    }

    private String getJWTToken(String hostname, String apiToken, LeanIXLogAction logAction, TaskListener listener) {

        String token;

        try {
            // Apply the hostname here to URL (from Credentials)
            URL url = new URL("https://" + hostname + "/services/mtm/v1/oauth2/token");
            String encoding = Base64.getEncoder().encodeToString(("apitoken:" + apiToken).getBytes(StandardCharsets.UTF_8));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + encoding);
            String postData = "grant_type=client_credentials";
            connection.setRequestProperty("Content-length", String.valueOf(postData.length()));
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
                return token;
            } catch (Exception e) {
                logAction.setResult(LeanIXLogAction.TOKEN_FAILED);
                listener.getLogger().println(LeanIXLogAction.TOKEN_FAILED + " Exception: " + e.getMessage());
            } finally {
                if (in != null)
                    in.close();
                output.close();
                connection.disconnect();
            }
        } catch (Exception e) {
            logAction.setResult(LeanIXLogAction.TOKEN_FAILED);
            listener.getLogger().println(LeanIXLogAction.TOKEN_FAILED + " Exception: " + e.getMessage());
        }
        return "";
    }

    @Symbol("leanIXMicroserviceIntelligence")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public static final String defaultLXManifestPath = "/lx-manifest.yml";
        public static final boolean defaultUseLeanIXConnector = true;
        private static final String[] DEPENDENCYMANAGERCHOICES = {"NPM", "GRADLE", "MAVEN"};
        private static Result jobresultchoicecentral = Result.SUCCESS;


        public FormValidation doCheckLxmanifestpath(@QueryParameter String value) {
            if (value.length() == 0)
                return FormValidation.error(Messages.LIXConnectorComBuilder_DescriptorImpl_errors_missingLXManifestPath());
            if (value.length() < 2)
                return FormValidation.warning(Messages.LIXConnectorComBuilder_DescriptorImpl_warnings_tooShort());
            return FormValidation.ok();
        }

        public FormValidation doCheckJobresultchoice(@QueryParameter String value) {
            if (!value.equals("") && !value.equals("FAILURE") && Result.fromString(value).equals(Result.FAILURE)) {
                return FormValidation.error(Messages.LIXConnectorComBuilder_DescriptorImpl_errors_severityLevelWrong());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckHostname(@QueryParameter String value) {
            if (value.length() == 0)
                return FormValidation.error(Messages.LIXConnectorComBuilder_DescriptorImpl_errors_missingHost());
            if (value.length() < 3)
                return FormValidation.warning(Messages.LIXConnectorComBuilder_DescriptorImpl_warnings_hostTooShort());
            return FormValidation.ok();
        }

        public FormValidation doCheckDependencymanager(@QueryParameter String value) {
            if (!value.equals("") && Arrays.stream(DEPENDENCYMANAGERCHOICES).noneMatch(value::equals)) {
                return FormValidation.error(Messages.LIXConnectorComBuilder_DescriptorImpl_errors_dependencyManagerChoiceWrong());
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

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.LIXConnectorComBuilder_DescriptorImpl_DisplayLXManifestPath();
        }

        public static Result getJobresultchoicecentral() {
            return jobresultchoicecentral;
        }

        public static void setJobresultchoicecentral(Result jobresultchoicecentral) {
            DescriptorImpl.jobresultchoicecentral = jobresultchoicecentral;
        }

    }
}