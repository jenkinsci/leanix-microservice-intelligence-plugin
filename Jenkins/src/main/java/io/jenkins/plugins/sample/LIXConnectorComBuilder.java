package io.jenkins.plugins.sample;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
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
import java.io.IOException;
import java.io.Serializable;


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
            LeanIXLogAction logAction = new LeanIXLogAction(lxmanifestpath);
            run.addAction(logAction);

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
                            setLxmanifestpath(pipeConfJson.get("path").toString());
                        }
                    }
                }
            } else {
                logAction.setLogMessage("Path to the manifest wasn't found. Please check your configuration!");
            }
            listener.getLogger().println("Your manifest path is " + lxmanifestpath + "!");
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
