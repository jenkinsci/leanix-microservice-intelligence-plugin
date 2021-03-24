package io.jenkins.plugins.leanixmi;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jenkins.model.Jenkins;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;


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
        readConfiguration();
    }

    public void readConfiguration() {
        Jenkins jenkinsInstance = Jenkins.getInstanceOrNull();
        if (jenkinsInstance == null)
            return;

        setFilePathsAndDirectories(jenkinsInstance);

        InputStream inputStream = null;
        try {
            if (new File(customFilePath).exists()) {
                inputStream = new FileInputStream(customFilePath);
                // need to use the way over UTF-8 because of different platforms and findbugs

            } else {

                URL defaultUrl = new URL(defaultFilePath);
                inputStream = defaultUrl.openStream();
            }

            readFrom(inputStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
            jsonConfigString = "";
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void readFrom(InputStream inputStream) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        Reader fileReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        Object obj = jsonParser.parse(fileReader);
        jsonConfig = obj;
        jsonConfigString = obj.toString().replaceAll("\\\\", "");
    }

    public String saveConfiguration(String jsonString) {
        setSavedCorrectly(false);
        checkCustomFileDir();
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
            // check if JSON can be converted to object
            isJsonObject(jsonString);

            //create file writer
            try (
                    FileOutputStream fileStream = new FileOutputStream(customFilePath);
                    OutputStreamWriter writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8)
            ) {
                setJsonCorrect(true);
                setSaveError(false);
                try {
                    writeTo(jsonString, writer);
                } catch (IOException e) {
                    e.printStackTrace();
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

    public void isJsonObject(String jsonString) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        jsonParser.parse(jsonString);
    }

    public void writeTo(String jsonString, OutputStreamWriter writer) throws IOException {
        // write to file
        writer.write(jsonString);
        writer.flush();
        setJsonConfigString(jsonString);
        setSavedCorrectly(true);
    }

    private void setFilePathsAndDirectories(Jenkins jenkins) {
        String url = jenkins.getRootUrl();
        if (url != null) {
            setDefaultFilePath(url.substring(0, url.length() - 1) + Jenkins.RESOURCE_PATH + "/plugin/leanix-microservice-intelligence/jsonpipelineconfiguration/defaultjsonconfig.json");
        }
        setCustomFileDirectory(jenkins.getRootDir() + "/leanix/jsonPipelineConfiguration");
        setCustomFilePath(jenkins.getRootDir() + "/leanix/jsonPipelineConfiguration/customJsonConfig.json");
    }

    // @SuppressFBWarnings: Error in the spotbugs version jenkins uses, if updated the annotation can maybe be removed
    // https://github.com/spotbugs/spotbugs/issues/518
    // TODO: Check the right permissions of the user with mkdirs() and catch File-Exception
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
    private void checkCustomFileDir() {
        File customDir = new File(customFileDirectory);
        if (!customDir.exists()) {
            customDir.mkdirs();
        }
    }

    private void setDefaultFilePath(String defaultPath) {
        defaultFilePath = defaultPath;
    }

    private void setCustomFilePath(String customPath) {
        customFilePath = customPath;
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

    private static String getSavingFilePath() {
        return "";
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

    public String getJsonIncorrectWarning() {
        return jsonIncorrectWarning;
    }

    public String getSaveErrorString() {
        return saveErrorString;
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
