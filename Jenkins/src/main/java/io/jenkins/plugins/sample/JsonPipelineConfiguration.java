package io.jenkins.plugins.sample;


import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

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
        Jenkins jenkins = Jenkins.getInstanceOrNull();

        if (jenkins != null) {
            String url = jenkins.getRootUrl();
            if (url != null) {
                setDefaultFilePath(url.substring(0, url.length() - 1) + Jenkins.RESOURCE_PATH + "/plugin/leanix_cicd/jsonpipelineconfiguration/defaultjsonconfig.json");
            }
            setCustomFileDirectory(jenkins.getRootDir() + "/jsonPipelineConfiguration");
            setCustomFilePath(jenkins.getRootDir() + "/jsonPipelineConfiguration/customJsonConfig.json");

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

                Reader fileReader = new InputStreamReader(inputStream, "UTF-8");
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
        try {
            checkCustomFileDir();
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
        try {
            FileWriter file = new FileWriter(customFilePath);
            try {

                // Constructs a FileWriter given a file name, using the platform's default charset

                JSONParser jsonParser = new JSONParser();
                Object JSONObj = jsonParser.parse(jsonString);
                file.write(JSONObj.toString());

            } catch (IOException e) {
                e.printStackTrace();
                return "Exception";

            } finally {

                try {
                    file.flush();
                    file.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return "Exception";
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return "Exception";
        }
        return "OK";
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

    public void checkCustomFileDir(){
        File customDir = new File(customFileDirectory);
        if (!customDir.exists()){
            customDir.mkdirs();
        }
    }

    public String getCustomFileDirectory() {
        return customFileDirectory;
    }

    public void setCustomFileDirectory(String customFileDirectory) {
        this.customFileDirectory = customFileDirectory;
    }
}
