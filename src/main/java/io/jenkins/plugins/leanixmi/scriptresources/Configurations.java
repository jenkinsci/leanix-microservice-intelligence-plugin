package io.jenkins.plugins.leanixmi.scriptresources;

public class Configurations {

    public static final String defaultPipelineConfigJSON = "{\n" +
            "  \"leanIXConfigurations\": {\n" +
            "    \"deploymentStageVarName\": \"stage\",\n" +
            "    \"deploymentVersionVarName\": \"version\",\n" +
            "    \"settings\": [\n" +
            "      {\n" +
            "        \"pipelines\": [\n" +
            "          \"pipeline1\",\n" +
            "          \"pipeline2\"\n" +
            "        ],\n" +
            "        \"path\": \"/other/lx-manifest.yml\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"pipelines\": [\n" +
            "          \"pipeline3\"\n" +
            "        ],\n" +
            "        \"path\": \"/lx-manifest.yml\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

}
