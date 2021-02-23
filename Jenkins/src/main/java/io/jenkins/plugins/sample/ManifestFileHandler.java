package io.jenkins.plugins.sample;

import org.json.simple.JSONValue;
import org.yaml.snakeyaml.Yaml;

public class ManifestFileHandler {


    String manifestYML = "";
    String manifestJSON = "";

    public ManifestFileHandler(String manifestPath) {
        retrieveManifestFromSCM(manifestPath);
    }

    private void retrieveManifestFromSCM(String manifestPath) {

        String yamlString = "";
        Yaml yaml= new Yaml();
        Object obj = yaml.load(yamlString);

        manifestJSON = JSONValue.toJSONString(obj);
    }

    public String getManifestYML() {
        return manifestYML;
    }

    public void setManifestYML(String manifestYML) {
        this.manifestYML = manifestYML;
    }

    public String getManifestJSON() {
        return manifestJSON;
    }

    public void setManifestJSON(String manifestJSON) {
        this.manifestJSON = manifestJSON;
    }

}
