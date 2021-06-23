package io.jenkins.plugins.leanixmi;


import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import okhttp3.*;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;

public class ConnectorHandler {

    public int sendFilesToConnector(String hostname, String jwtToken, String deploymentVersion, String deploymentStage, String dependencyManager, File projectDependencies, String manifestJSON, LeanIXLogAction logAction, TaskListener listener) {


        JSONObject dataObj = new JSONObject();
        dataObj.put("version", deploymentVersion);
        dataObj.put("stage", deploymentStage);
        if(!dependencyManager.equals("")) {
            dataObj.put("dependencyManager", dependencyManager);
        }

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


            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("manifest", "manifest", RequestBody.create(MediaType.parse("text/plain"), manifestJSON))
                    .addFormDataPart("data", "data",
                            RequestBody.create(MediaType.parse("application/json"), dataObj.toJSONString()));

            if (projectDependencies != null && !dependencyManager.equals("")) {
                builder.addFormDataPart("", projectDependencies.getAbsolutePath(),
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File(projectDependencies.getAbsolutePath())));
            }

            RequestBody body = builder.build();


            Request request = new Request.Builder()
                    .url(httpUrl)
                    .post(body)
                    .addHeader("content-type", "multipart/form-data;")
                    .addHeader("accept", "*/*")
                    .addHeader("Content-Type", "multipart/form-data")
                    .addHeader("Authorization", "Bearer " + jwtToken)
                    .addHeader("cache-control", "no-cache")
                    .build();

            Response response = client.newCall(request).execute();
            String responseJSON = response.body().string();
            int responseCode = response.code();

            if (responseCode < 200 || responseCode > 308) {
                logAction.setResult(LeanIXLogAction.API_CALL_FAILED + "\n API responded with \n Response code: " + responseCode + " - " + response.message() + "\n Response message: " + responseJSON);
                listener.getLogger().println(LeanIXLogAction.API_CALL_FAILED + "\n API responded with \n Response code: " + responseCode + " - " + response.message() + "\n Response message: " + responseJSON);
            }else{
                listener.getLogger().println("The LeanIX API was called and responded with \n Response code: " + responseCode + " - " + response.message() + "\n Response message: " + responseJSON);
            }


            return response.code();
        } catch (Exception e) {
            logAction.setResult(LeanIXLogAction.API_CALL_EXCEPTION + "\n Exception: " + e.getMessage());
            listener.getLogger().println(LeanIXLogAction.API_CALL_EXCEPTION + "\n Exception: " + e.getMessage());
        } finally {
            if (responseBody != null)
                responseBody.close();
        }
        return 0;
    }
}
