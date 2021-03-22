package io.jenkins.plugins.leanixmi;

import com.squareup.okhttp.*;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;

public class ConnectorHandler {

    public int sendFilesToConnector(String hostname, String jwtToken, String deploymentVersion, String deploymentStage, String dependencyManager, File projectDependencies, String manifestJSON) throws IOException {

        String boundary = Long.toString(System.currentTimeMillis());

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


            // TODO: Refactor this and create the body by building the content in a nice way
            MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary" + boundary);
            RequestBody body = RequestBody.create(mediaType, "------WebKitFormBoundary" + boundary +
                    "\r\nContent-Disposition: form-data; name=\"manifest\"\r\n\r\n" + manifestJSON + "\r\n"
                    + "------WebKitFormBoundary" + boundary + "\r\nContent-Disposition: form-data; " +
                    "name=\"dependencies\"\r\nContent-Type: application/json\r\n\r\n" + dataObj + "\r\n"
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

}
