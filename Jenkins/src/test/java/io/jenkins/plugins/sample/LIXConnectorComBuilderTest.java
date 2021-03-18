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

    final String lxManifestPath = "Please specify this path in the plugin configuration.";
    final String deployment = "Please specify this variable name in the plugin configuration. And set the value into jenkins environment.";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        LIXConnectorComBuilder builder = new LIXConnectorComBuilder();
        project.getBuildersList().add(builder);
        project = jenkins.configRoundtrip(project);
        LIXConnectorComBuilder testBuilder = new LIXConnectorComBuilder();
        testBuilder.setLxmanifestpath(lxManifestPath);
        testBuilder.setHostname("");
        testBuilder.setApitoken("");
        // testBuilder.setDeploymentstage(deployment);
        // testBuilder.setDeploymentversion(deployment);
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
        lhs.setApitoken("");
       // lhs.setDeploymentstage(deployment);
        // lhs.setDeploymentversion(deployment);
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
        jenkins.assertLogContains("Path to the manifest wasn't found", build);
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
        // String expectedString = "Your manifest path is " + lxManifestPath + "!";
        String expectedString = "Path to the manifest wasn't found";
        jenkins.assertLogContains(expectedString, completedBuild);
    }

}