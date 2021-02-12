package io.jenkins.plugins.sample;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonPipelineConfiguration {

    private String jsonConfig;
    private String defaultFilePath;
    private String customFilePath;
    private String customFileDirectory;

    public JsonPipelineConfiguration() {

        if (setFilePathsAndDirectories()) {
            //JSON parser object to parse read file
            JSONParser jsonParser = new JSONParser();

            try {
                InputStream inputStream;

                if (new File(customFilePath).exists()) {
                    inputStream = new FileInputStream(customFilePath);
                    // need to use the way over UTF-8 because of different platforms and findbugs

                } else {

                    URL defaultUrl = new URL(defaultFilePath);
                    inputStream = defaultUrl.openStream();
                }

                Reader fileReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                //Read JSON file
                Object obj = jsonParser.parse(fileReader);
                jsonConfig = obj.toString();

            } catch (FileNotFoundException e) {

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();

            }
        }
    }

    public String saveConfiguration(String jsonString) {

        jsonConfig = jsonString;

        //check, if directory and file exist
        if (checkCustomFileDir()) {

            try {

                File fileCheck = new File(customFilePath);
                if (fileCheck.createNewFile()) {
                    System.out.println("File created: " + fileCheck.getName());
                } else {
                    System.out.println("File already exists.");
                }
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
                return "Exception";
            }


            //write to the file
            try (
                    FileOutputStream fileStream = new FileOutputStream(customFilePath);
                    OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF-8");
            ) {

                try {

                    JSONParser jsonParser = new JSONParser();
                    Object JSONObj = jsonParser.parse(jsonString);
                    writer.write(JSONObj.toString());
                    writer.flush();
                    writer.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    writer.close();
                    return "Exception";
                }
            } catch (final Exception e) {
                e.printStackTrace();
                return "Exception";
            }
            return "OK";
        }
        return "Exception";
    }

    // @SuppressFBWarnings: Error in the spotbugs version jenkins uses, if updated it can maybe be removed
    // https://github.com/spotbugs/spotbugs/issues/518
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
    public boolean checkCustomFileDir() {
        File customDir = new File(customFileDirectory);
        try {
            if (!customDir.exists()) {
                customDir.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean setFilePathsAndDirectories() {
        Jenkins jenkins = Jenkins.getInstanceOrNull();

        if (jenkins != null) {
            String url = jenkins.getRootUrl();
            if (url != null) {
                setDefaultFilePath(url.substring(0, url.length() - 1) + Jenkins.RESOURCE_PATH + "/plugin/leanix_cicd/jsonpipelineconfiguration/defaultjsonconfig.json");
            }
            setCustomFileDirectory(jenkins.getRootDir() + "/jsonPipelineConfiguration");
            setCustomFilePath(jenkins.getRootDir() + "/jsonPipelineConfiguration/customJsonConfig.json");
            return true;
        }
        return false;
    }


    private void setDefaultFilePath(String defaultPath) {
        defaultFilePath = defaultPath;
    }

    private void setCustomFilePath(String customPath) {
        customFilePath = customPath;
    }

    private static String getSavingFilePath() {

        return "";
    }

    public String getJsonConfig() {
        return jsonConfig;
    }

    public void setJsonConfig(String jsonConfig) {
        this.jsonConfig = jsonConfig;
    }

    public String getCustomFileDirectory() {
        return customFileDirectory;
    }

    public void setCustomFileDirectory(String customFileDirectory) {
        this.customFileDirectory = customFileDirectory;
    }
}
