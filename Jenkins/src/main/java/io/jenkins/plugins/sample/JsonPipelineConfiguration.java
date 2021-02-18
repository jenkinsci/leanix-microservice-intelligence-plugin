package io.jenkins.plugins.sample;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jenkins.model.Jenkins;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;


import org.json.simple.parser.*;


public class JsonPipelineConfiguration {

    private String jsonConfigString;
    private Object jsonConfig;
    private String defaultFilePath;
    private String customFilePath;
    private String customFileDirectory;
    private static final String jsonIncorrectWarning = "There seems to be an error in your JSON string, please check it.";
    private static final String saveErrorString = "An error occurred while saving. Please try again.";
    private boolean jsonCorrect = true;
    private boolean saveError = false;
    private boolean savedCorrectly = false;


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
                jsonConfig = obj;
                jsonConfigString = obj.toString().replaceAll("\\\\", "");

            } catch (FileNotFoundException e) {

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
                jsonConfigString = "";
            }
        }
    }

    public String saveConfiguration(String jsonString) {
        setSavedCorrectly(false);

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

            try {
                // check JSON and convert it to object
                JSONParser jsonParser = new JSONParser();
                Object JSONObj = jsonParser.parse(jsonString);

                //create file writer
                try (
                        FileOutputStream fileStream = new FileOutputStream(customFilePath);
                        OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF-8");
                ) {

                    try {
                        setJsonCorrect(true);
                        setSaveError(false);

                        // write to file
                        writer.write(JSONObj.toString());
                        writer.flush();
                        writer.close();
                        setJsonConfigString(jsonString);
                        setSavedCorrectly(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                        writer.close();
                        fileStream.close();
                        setSaveError(true);
                        return "Exception";
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    return "Exception";
                }
            } catch (ParseException e) {
                System.out.println("JSON wrong");
                setJsonCorrect(false);
                setJsonConfigString(jsonString);
            }
            return "OK";
        }
        return "Exception";
    }

    // @SuppressFBWarnings: Error in the spotbugs version jenkins uses, if updated the annotation can maybe be removed
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

    public String getJsonConfigString() {
        return jsonConfigString;
    }

    public void setJsonConfigString(String jsonConfigString) {
        this.jsonConfigString = jsonConfigString;
    }

    public String getCustomFileDirectory() {
        return customFileDirectory;
    }

    public void setCustomFileDirectory(String customFileDirectory) {
        this.customFileDirectory = customFileDirectory;
    }

    public String getJsonIncorrectWarning() {
        return jsonIncorrectWarning;
    }

    public String getSaveErrorString() {
        return saveErrorString;
    }

    public boolean getJsonCorrect() {
        return jsonCorrect;
    }

    public void setJsonCorrect(boolean jsonCorrect) {
        this.jsonCorrect = jsonCorrect;
    }

    public boolean getSaveError() {
        return saveError;
    }

    public void setSaveError(boolean saveError) {
        this.saveError = saveError;
    }


    public Object getJsonConfig() {
        return jsonConfig;
    }

    public void setJsonConfig(Object jsonConfig) {
        this.jsonConfig = jsonConfig;
    }

    public boolean getSavedCorrectly() {
        return savedCorrectly;
    }

    public void setSavedCorrectly(boolean savedCorrectly) {
        this.savedCorrectly = savedCorrectly;
    }

}
