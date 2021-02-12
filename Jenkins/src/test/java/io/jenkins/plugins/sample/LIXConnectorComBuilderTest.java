package io.jenkins.plugins.sample;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class LIXConnectorComBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String lxManifestPath = "Bobby";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new LIXConnectorComBuilder(lxManifestPath));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(new LIXConnectorComBuilder(lxManifestPath), project.getBuildersList().get(0));
    }

    @Test
    public void testConfigRoundtripUseLeanIXConnector() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        LIXConnectorComBuilder builder = new LIXConnectorComBuilder(lxManifestPath);
        builder.setUseLeanIXConnector(true);
        project.getBuildersList().add(builder);
        project = jenkins.configRoundtrip(project);

        LIXConnectorComBuilder lhs = new LIXConnectorComBuilder(lxManifestPath);
        lhs.setUseLeanIXConnector(true);
        jenkins.assertEqualDataBoundBeans(lhs, project.getBuildersList().get(0));
    }

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        LIXConnectorComBuilder builder = new LIXConnectorComBuilder(lxManifestPath);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Your manifest path is " + lxManifestPath, build);
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript
                = "node {\n"
                + "  log '" + lxManifestPath + "'\n"
                + "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "Your manifest path is " + lxManifestPath + "!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }

}