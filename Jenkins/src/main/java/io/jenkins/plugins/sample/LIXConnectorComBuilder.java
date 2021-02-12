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


public class LIXConnectorComBuilder extends Builder implements SimpleBuildStep {

    private final String lxManifestPath;
    private boolean useLeanIXConnector = true;

    @DataBoundConstructor
    public LIXConnectorComBuilder(String lxManifestPath) {
        if (lxManifestPath == null || lxManifestPath.equals("")) {
            this.lxManifestPath = "/lx-manifest.yml";
        } else {
            this.lxManifestPath = lxManifestPath;
        }
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

        public FormValidation doCheckLXManifestPath(@QueryParameter String value, @QueryParameter boolean useFrench)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.LIXConnectorComBuilder_DescriptorImpl_errors_missingLXManifestPath());
            if (value.length() < 4)
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
