package io.jenkins.plugins.leanixmi;

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

import javax.servlet.ServletException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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
    private String apitoken;
    private String jobresultchoice = "";
    private String deploymentstage;
    private String deploymentversion;
    private static final String defaultVersion = "Default version number used is BUILD_ID ";

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

    public String getLxmanifestpath() {
        return lxmanifestpath;
    }


    public String getDependencymanager() {
        return dependencymanager;
    }

    @DataBoundSetter
    public void setDependencymanager(String dependencymanager) {
        if (Arrays.stream(DescriptorImpl.DEPENDENCYMANAGERCHOICES).anyMatch(dependencymanager::equals)) {
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

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {

        if (getUseleanixconnector()) {

            if (jobresultchoice == null || jobresultchoice.equals("")) {
                jobresultchoice = DescriptorImpl.getJobresultchoicecentral().toString();
            }

            boolean configFound;
            LeanIXLogAction logAction = new LeanIXLogAction("Something went wrong. Please review your LeanIX-Configuration!");


            Job job = run.getParent();
            configFound = findJSONPipelineConfig(job);
            if (!configFound) {
                logAction.setLxManifestPath(LeanIXLogAction.MANIFEST_NOTFOUND);
                logAction.setResult(LeanIXLogAction.MANIFEST_NOTFOUND);
                listener.getLogger().println(LeanIXLogAction.MANIFEST_NOTFOUND);
                setLxmanifestpath(LeanIXLogAction.MANIFEST_NOTFOUND);
                run.setResult(LIXConnectorComBuilder.DescriptorImpl.getJobresultchoicecentral());
            } else {
                listener.getLogger().println("Your manifest path is " + lxmanifestpath + "!");
                logAction.setLxManifestPath(lxmanifestpath);

                ManifestFileHandler manifestFileHandler = new ManifestFileHandler(jobresultchoice);
                File folderPathFile = new File(Jenkins.get().getRootDir() + "/leanix/git/" + job.getDisplayName() + "/checkout");
                boolean manifestFileFound = manifestFileHandler.retrieveManifestJSONFromSCM(lxmanifestpath, job, run, launcher, listener, logAction, folderPathFile);
                DependencyHandler dependencyHandler = new DependencyHandler();



                String stage = env.get(deploymentstage);
                String version = env.get(deploymentversion);
                if (stage != null && !stage.equals("")) {
                    listener.getLogger().println("Your deployment stage is " + stage + ".");
                    logAction.setStage(stage);
                } else {
                    logAction.setStage(LeanIXLogAction.STAGE_NOTSET);
                    listener.getLogger().println(LeanIXLogAction.STAGE_NOTSET);
                    setDeploymentstage(LeanIXLogAction.STAGE_NOTSET);
                }
                if (version != null && !version.equals("")) {
                    listener.getLogger().println("Your deployment version is " + version + ".");
                    logAction.setVersion(version);
                } else {
                    version = env.get("BUILD_ID");
                    logAction.setStage(LeanIXLogAction.VERSION_NOTSET);
                    listener.getLogger().println(LeanIXLogAction.VERSION_NOTSET);
                    setDeploymentstage(LeanIXLogAction.VERSION_NOTSET);
                    listener.getLogger().println(defaultVersion + version + ".");
                    logAction.setVersion(version);
                }


                // If SCM was checked out correctly
                if (run.getResult() != null && manifestFileFound) {
                    /*File projectDependencies = */
                    dependencyHandler.createProjectDependenciesFile(dependencymanager, folderPathFile);

                    String host = this.getHostname();
                    String jwtToken = getJWTToken(host);
                    if (jwtToken != null && !jwtToken.isEmpty()) {
                        int responseCode = manifestFileHandler.sendFileToConnector(host, jwtToken, version, stage, dependencymanager);
                        if (responseCode < 200 || responseCode > 308) {
                            logAction.setResult(LeanIXLogAction.API_CALL_FAILED);
                            run.setResult(DescriptorImpl.getJobresultchoicecentral());
                        }
                    } else {
                        run.setResult(Result.fromString(getJobresultchoice()));
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

    private String getJWTToken(String hostname) {
        // test for the use of API-Token and requesting JWT-Token
        String apiToken = this.getApitoken();
        String token;

        try {
            // TODO: Apply the hostname here to URL (from Credentials)
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
            } finally {
                if (in != null)
                    in.close();
                output.close();
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
        return "";
    }

    @Symbol("leanIXMicroserviceIntelligence")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public static final String defaultLXManifestPath = "/lx-manifest.yml";
        public static final boolean defaultUseLeanIXConnector = true;
        private static final String[] DEPENDENCYMANAGERCHOICES = {"NPM", "GRADLE", "MAVEN"};
        private static Result jobresultchoicecentral = Result.SUCCESS;



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

        public FormValidation doCheckDependencymanager(@QueryParameter String value)
                throws IOException, ServletException {
            if (!value.equals("") && !Arrays.stream(DEPENDENCYMANAGERCHOICES).anyMatch(value::equals)) {
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