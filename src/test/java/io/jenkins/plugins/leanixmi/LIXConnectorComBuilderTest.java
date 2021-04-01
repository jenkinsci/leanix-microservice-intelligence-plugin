package io.jenkins.plugins.leanixmi;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.util.Secret;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class LIXConnectorComBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String pathNotFound = "The manifest file could not be found in your Source Code Management System, please check that the path you specified is correct.";
    final String lxManifestPath = "Please specify this path in the plugin configuration.";
    final String configFileNotFound = "The configuration file for the LeanIX Plugin could not be found.";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        LIXConnectorComBuilder builder = new LIXConnectorComBuilder();
        project.getBuildersList().add(builder);
        project = jenkins.configRoundtrip(project);
        LIXConnectorComBuilder testBuilder = new LIXConnectorComBuilder();
        testBuilder.setLxmanifestpath(lxManifestPath);
        testBuilder.setHostname("");
        testBuilder.setApitoken(Secret.fromString(""));
        jenkins.assertEqualDataBoundBeans(testBuilder, project.getBuildersList().get(0));
    }

    @Test
    public void testConfigRoundtripUseLeanIXConnector() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        LIXConnectorComBuilder builder = new LIXConnectorComBuilder();
        project.getBuildersList().add(builder);
        project = jenkins.configRoundtrip(project);
        LIXConnectorComBuilder lhs = new LIXConnectorComBuilder();
        lhs.setLxmanifestpath(lxManifestPath);
        lhs.setHostname("");
        lhs.setApitoken(Secret.fromString(""));
        jenkins.assertEqualDataBoundBeans(lhs, project.getBuildersList().get(0));
    }

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        LIXConnectorComBuilder builder = new LIXConnectorComBuilder();
        builder.setLxmanifestpath("/lx-manifest.yml");
        builder.setUseleanixconnector(true);
        project.getBuildersList().add(builder);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains(configFileNotFound, build);
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript
                = "node {\n"
                + "  leanIXMicroserviceIntelligence lxmanifestpath: '/lx-manifest.yml', useleanixconnector: true" + "\n"
                + "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkins.assertLogContains(configFileNotFound, completedBuild);
    }

}