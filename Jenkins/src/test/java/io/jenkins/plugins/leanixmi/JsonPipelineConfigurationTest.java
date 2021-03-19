package io.jenkins.plugins.leanixmi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class JsonPipelineConfigurationTest {

    JsonPipelineConfiguration underTest = new JsonPipelineConfiguration();

    @Test
    public void testReadFrom() {
        String jsonString = "{\n" +
                "  \"leanIXConfigurations\":[\n" +
                "\n \t" +
                "    {\n" +
                "      \"pipelines\":[\n" +
                "        \"pipeline1\",\n" +
                "        \"pipeline2\"\n" +
                "      ],\n" +
                "      \"path\":\"/other/lx-manifest.yaml\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"pipelines\":[\n" +
                "        \"pipeline3\"\n" +
                "      ],\n" +
                "      \"path\":\"/lx-manifest.yml\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        InputStream is = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
        try {
            underTest.readFrom(is);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String js = "{\n" +
                "  \"leanIXConfigurations\":[\n" +
                "    {\n" +
                "      \"pipelines\":[\n" +
                "        \"pipeline1\",\n" +
                "        \"pipeline2\"\n" +
                "      ],\n" +
                "      \"path\":\"/other/lx-manifest.yaml\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"pipelines\":[\n" +
                "        \"pipeline3\"\n" +
                "      ],\n" +
                "      \"path\":\"/lx-manifest.yml\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        ObjectMapper mapper = new ObjectMapper();
        try {
            assertEquals(mapper.readTree(underTest.getJsonConfigString()), mapper.readTree(js));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWriteTo() {
        String jsonString = "{\n" +
                "  \"leanIXConfigurations\":[\n" +
                "    {\n" +
                "      \"pipelines\":[\n" +
                "        \"pipeline1\",\n" +
                "        \"pipeline2\"\n" +
                "      ],\n" +
                "      \"path\":\"/dir/lx-manifest.yaml\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"pipelines\":[\n" +
                "        \"pipeline3\"\n" +
                "      ],\n" +
                "      \"path\":\"/lx-manifest.yml\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        File file = new File("./test.txt");
        FileOutputStream fileStream = null;
        try {
            fileStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (fileStream != null) {
            OutputStreamWriter writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
            try {
                underTest.writeTo(jsonString, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assertTrue(underTest.getSavedCorrectly());
        }

        if (file.exists()) {
            Path path = Paths.get("test.txt");
            byte[] bytes = new byte[0];
            try {
                bytes = Files.readAllBytes(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String str = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(jsonString, str);
        }
    }

    @Test
    public void testIsJsonObject() {
        String jsonString1 = "{\n" +
                "  \"leanIXConfigurations\":[\n" +
                "    {\n" +
                "      \"pipelines\":[\n" +
                "        \"pipeline1\",\n" +
                "        \"pipeline2\"\n" +
                "      ],\n" +
                "      \"path\":\"/dir/lx-manifest.yaml\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"pipelines\":[\n" +
                "        \"pipeline3\"\n" +
                "      ],\n" +
                "      \"path\":\"/lx-manifest.yml\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        String jsonString2 = "{\n" +
                "  \"leanIXConfigurations\":\n" +
                "    {\n" +
                "      \"pipelines\":[\n" +
                "        \"pipeline1\",\n" +
                "        \"pipeline2\"\n" +
                "      ],\n" +
                "      \"path\":\"/dir/lx-manifest.yaml\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"pipelines\":[\n" +
                "        \"pipeline3\"\n" +
                "      ],\n" +
                "      \"path\":\"/lx-manifest.yml\"\n" +
                "    }\n" +
                "  \n" +
                "}";
        boolean throw1 = false;
        boolean throw2 = false;
        try {
            underTest.isJsonObject(jsonString1);
        } catch (ParseException e) {
            throw1 = true;
        }
        try {
            underTest.isJsonObject(jsonString2);
        } catch (ParseException e) {
            throw2 = true;
        }

        assertFalse(throw1);
        assertTrue(throw2);
    }
}