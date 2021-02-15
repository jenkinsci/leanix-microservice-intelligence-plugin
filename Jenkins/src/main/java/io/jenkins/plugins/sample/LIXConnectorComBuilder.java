package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Serializable;


@Extension
public class LIXConnectorComBuilder extends Builder implements SimpleBuildStep, Serializable {

    private String lxManifestPath = "/lx-manifest.yml";
    private boolean useLeanIXConnector = true;

    @DataBoundConstructor
    public LIXConnectorComBuilder() {
    }

    public String getLxManifestPath() {
        return lxManifestPath;
    }

    public boolean isUseLeanIXConnector() {
        return useLeanIXConnector;
    }

    @DataBoundSetter
    public void setUseLeanIXConnector(boolean useLeanIXConnector) {
        this.useLeanIXConnector = useLeanIXConnector;
    }

    @DataBoundSetter
    public void setLxManifestPath(String lxManifestPath) {
        this.lxManifestPath = lxManifestPath;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {

        if (isUseLeanIXConnector()) {
            run.addAction(new LeanIXLogAction(lxManifestPath));
            listener.getLogger().println("Your manifest path is " + lxManifestPath + "!");
        }

    }

    @Symbol("log")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckLxmanifestpath(@QueryParameter String value, @QueryParameter boolean useleanixconnector)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.LIXConnectorComBuilder_DescriptorImpl_errors_missingLXManifestPath());
            if (value.length() < 2)
                return FormValidation.warning(Messages.LIXConnectorComBuilder_DescriptorImpl_warnings_tooShort());
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
