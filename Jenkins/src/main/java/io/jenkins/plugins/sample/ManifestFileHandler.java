package io.jenkins.plugins.sample;

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
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ManifestFileHandler {


    String manifestJSON = "";

    public ManifestFileHandler() {

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
                    manifestJSON = getManifestFileFromFolder(folderPathFile, manifestPath);
                    if (!manifestJSON.equals("")) {
                        // backslashes do not work with the API, remove them
                        manifestJSON = manifestJSON.replaceAll("\\\\", "");
                        return true;
                    } else {
                        setBuildFailedSCM(run, logAction);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    setBuildFailedSCM(run, logAction);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    setBuildFailedSCM(run, logAction);
                } catch (ParseException e) {
                    e.printStackTrace();
                    setBuildFailedSCM(run, logAction);
                }
            } else {
                setBuildFailedSCM(run, logAction);
            }
        } else {
            setBuildFailedSCM(run, logAction);
        }
        return false;
    }

    public int sendFileToConnector(String jwtToken, String deploymentVersion, String deploymentStage) throws IOException {


        String boundary = Long.toString(System.currentTimeMillis());
        // String postData = "------WebKitFormBoundary" + boundary + "\r\nContent-Disposition: form-data; name=\"manifest\"\r\n\r\n" + manifestJSON + "\r\n------WebKitFormBoundary" + boundary + "--";

        try {

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary" + boundary);
            RequestBody body = RequestBody.create(mediaType, "------WebKitFormBoundary" + boundary + "\r\nContent-Disposition: form-data; name=\"manifest\"\r\n\r\n" + manifestJSON + "\r\n------WebKitFormBoundary" + boundary + "--");
            Request request = new Request.Builder()
                    .url("https://app.leanix.net/services/cicd-connector/v2/deployment?deploymentVersion=" + deploymentVersion + "&deploymentStage=" + deploymentStage)
                    .post(body)
                    .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary" + boundary)
                    .addHeader("accept", "*/*")
                    .addHeader("Content-Type", "multipart/form-data")
                    .addHeader("Authorization", "Bearer " + jwtToken)
                    .addHeader("cache-control", "no-cache")
                    .build();

            Response response = client.newCall(request).execute();

            return response.code();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    private String getManifestFileFromFolder(File folderPath, String manifestPath) throws IOException, ParseException {
        String fullPath = folderPath.getAbsolutePath() + manifestPath;
        InputStream inputStream = null;

        if (new File(fullPath).exists()) {
            inputStream = new FileInputStream(fullPath);

            Reader fileReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Yaml yaml = new Yaml();
            Object obj = yaml.load(fileReader);
            return JSONValue.toJSONString(obj);

        } else {
            return "";
        }
    }

    private void setBuildFailedSCM(Run run, LeanIXLogAction logAction) {
        run.setResult(LIXConnectorComBuilder.DescriptorImpl.getJobresultchoice());
        logAction.setResult(LeanIXLogAction.SCM_FAILED);
    }

    public String getManifestJSON() {
        return manifestJSON;
    }

    public void setManifestJSON(String manifestJSON) {
        this.manifestJSON = manifestJSON;
    }

}