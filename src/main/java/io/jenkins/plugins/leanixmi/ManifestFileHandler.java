package io.jenkins.plugins.leanixmi;

import com.squareup.okhttp.*;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;
import jenkins.model.Jenkins;
import jenkins.triggers.SCMTriggerItem;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ManifestFileHandler {


    String manifestJSON = "";
    private String jobresultchoice = "";

    public ManifestFileHandler(String jrs) {
        jobresultchoice = jrs;
    }

    public boolean retrieveManifestJSONFromSCM(String manifestPath, Job job, Run run, Launcher launcher, TaskListener listener, LeanIXLogAction logAction) {


        // dealing with the SCM (see ManifestFile - Class)

        run.setResult(Result.SUCCESS);
        logAction.setResult(LeanIXLogAction.SUCCESS);
        SCMTriggerItem s = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job);
        Jenkins jenkins = Jenkins.get();
        File changelog = new File(jenkins.getRootDir() + "/leanix/changelog");
        SCMRevisionState scmRS = null;
        File folderPathFile = new File(jenkins.getRootDir() + "/leanix/git/" + job.getDisplayName() + "/checkout");
        FilePath filePath = new FilePath(folderPathFile);
        if (s != null) {
            ArrayList<SCM> scms = new ArrayList<>(s.getSCMs());
            if (!scms.isEmpty()) {
                SCM scmItm = scms.get(0);
                try {
                    scmItm.checkout(run, launcher, filePath, listener, changelog, scmRS);
                    manifestJSON = getManifestFileFromFolder(folderPathFile, manifestPath, run, logAction);
                    if (!manifestJSON.equals("")) {
                        // backslashes do not work with the API, remove them
                        manifestJSON = manifestJSON.replaceAll("\\\\", "");
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    setBuildFailed(run, logAction, LeanIXLogAction.SCM_FAILED);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    setBuildFailed(run, logAction, LeanIXLogAction.SCM_FAILED);
                } catch (ParseException e) {
                    e.printStackTrace();
                    setBuildFailed(run, logAction, LeanIXLogAction.MANIFEST_WRONG);
                }
            } else {
                setBuildFailed(run, logAction, LeanIXLogAction.SCM_FAILED);
            }
        } else {
            setBuildFailed(run, logAction, LeanIXLogAction.SCM_FAILED);
        }
        return false;
    }

    public int sendFileToConnector(String hostname, String jwtToken, String deploymentVersion, String deploymentStage) throws IOException {

        String boundary = Long.toString(System.currentTimeMillis());

        JSONObject dataObj = new JSONObject();
        dataObj.put("version", deploymentVersion);
        dataObj.put("stage", deploymentStage);

        ResponseBody responseBody = null;
        try {

            OkHttpClient client = new OkHttpClient();
            HttpUrl httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(hostname)
                    .addPathSegment("services")
                    .addPathSegment("cicd-connector")
                    .addPathSegment("v2")
                    .addPathSegment("deployment")
                    .build();


            MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary" + boundary);
            RequestBody body = RequestBody.create(mediaType, "------WebKitFormBoundary" + boundary +
                    "\r\nContent-Disposition: form-data; name=\"manifest\"\r\n\r\n" + manifestJSON + "\r\n"
                    + "------WebKitFormBoundary" + boundary + "\r\nContent-Disposition: form-data; " +
                    "name=\"data\"\r\nContent-Type: application/json\r\n\r\n" + dataObj + "\r\n------WebKitFormBoundary"
                    + boundary + "--");
            Request request = new Request.Builder()
                    .url(httpUrl)
                    .post(body)
                    .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary" + boundary)
                    .addHeader("accept", "*/*")
                    .addHeader("Content-Type", "multipart/form-data")
                    .addHeader("Authorization", "Bearer " + jwtToken)
                    .addHeader("cache-control", "no-cache")
                    .build();

            Response response = client.newCall(request).execute();
            responseBody = response.body();
            return response.code();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (responseBody != null)
                responseBody.close();
        }

    }

    private String getManifestFileFromFolder(File folderPath, String manifestPath, Run run, LeanIXLogAction logAction) throws IOException, ParseException {
        String fullPath = folderPath.getAbsolutePath() + manifestPath;
        InputStream inputStream;

        if (new File(fullPath).exists()) {
            inputStream = new FileInputStream(fullPath);

            // reading in the YAML and checking that it is valid YAML and the outcome is valid JSON
            Reader fileReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Yaml yaml = new Yaml();
            String jsonString = "";

            try {
                Object obj = yaml.load(fileReader);
                jsonString = JSONValue.toJSONString(obj);
                JSONParser jsonParser = new JSONParser();
                jsonParser.parse(jsonString);
            } catch (YAMLException e) {
                setBuildFailed(run, logAction, LeanIXLogAction.MANIFEST_WRONG);
            }

            return jsonString;

        } else {
            setBuildFailed(run, logAction, LeanIXLogAction.MANIFEST_NOTFOUND);
            return "";
        }
    }

    private void setBuildFailed(Run run, LeanIXLogAction logAction, String logActionString) {
        run.setResult(Result.fromString(jobresultchoice));
        logAction.setResult(logActionString);
    }

    public String getManifestJSON() {
        return manifestJSON;
    }

    public void setManifestJSON(String manifestJSON) {
        this.manifestJSON = manifestJSON;
    }

}