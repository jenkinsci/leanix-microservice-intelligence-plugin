package io.jenkins.plugins.leanixmi;


import okhttp3.*;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;

public class ConnectorHandler {

    public int sendFilesToConnector(String hostname, String jwtToken, String deploymentVersion, String deploymentStage, String dependencyManager, File projectDependencies, String manifestJSON) throws IOException {

        // String boundary = Long.toString(System.currentTimeMillis());

        JSONObject dataObj = new JSONObject();
        dataObj.put("version", deploymentVersion);
        dataObj.put("stage", deploymentStage);
        dataObj.put("dependencyManager", dependencyManager);
        // String dataObjectString = dataObj.toJSONString();

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
                    .addFormDataPart("manifest", "manifest",
                            RequestBody.create(MediaType.parse("text/plain"), manifestJSON))
                    .addFormDataPart("data", "data",
                            RequestBody.create(MediaType.parse("application/json"), dataObj.toJSONString()));
            // TODO: This part doesn't work yet!
            if (projectDependencies != null) {
              // builder.addFormDataPart("dependencies", null, RequestBody.create(MediaType.parse(""), projectDependencies));
                builder.addFormDataPart("",projectDependencies.getAbsolutePath(),
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
}
