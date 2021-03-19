package io.jenkins.plugins.leanixmi;

import hudson.model.Result;
import jenkins.model.Jenkins;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class SettingsHandler {

    private String settingsDirPath = "/leanix/settings";
    private String settingsFileName = "settings.json";
    private JSONObject settingsObj = new JSONObject();

    public SettingsHandler() {
        checkAndCreateSettingsFile();
        readInSettingsObj();
    }

    public void saveSetting(String key, String value) {
        String filePath = Jenkins.get().getRootDir() + settingsDirPath + "/" + settingsFileName;
        settingsObj.put(key, value);

        try {
            File fileCheck = new File(filePath);
            if (fileCheck.createNewFile()) {
                System.out.println("File created: " + fileCheck.getName());
            }
            FileOutputStream fileStream = new FileOutputStream(filePath);
            OutputStreamWriter writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
            writer.write(settingsObj.toJSONString());
            writer.flush();
            fileStream.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

    private void checkAndCreateSettingsFile() {
        Jenkins jenkins = Jenkins.get();
        File settingsDir = new File(jenkins.getRootDir() + settingsDirPath);
        if (!settingsDir.exists()) {
            if (settingsDir.mkdirs()) {
                System.out.println("New settings directory created.");
            }
        }
        File settingsFile = new File(Jenkins.get().getRootDir() + settingsDirPath + "/" + settingsFileName);
        try {
            if (settingsFile.createNewFile()) {
                saveSetting("jobresultchoice", Result.SUCCESS.toString());
                System.out.println("New settings file created.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readInSettingsObj() {
        String filePath = Jenkins.get().getRootDir() + settingsDirPath + "/" + settingsFileName;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            readFrom(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void readFrom(InputStream inputStream) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        Reader fileReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        Object obj = jsonParser.parse(fileReader);
        settingsObj = (JSONObject) obj;
    }

    public JSONObject getSettingsObj() {
        return settingsObj;
    }

    public void setSettingsObj(JSONObject settingsObj) {
        this.settingsObj = settingsObj;
    }
}
