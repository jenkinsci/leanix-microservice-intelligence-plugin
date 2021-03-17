package io.jenkins.plugins.sample;

import jenkins.model.Jenkins;
import java.io.File;

public class DependencyHandler {


    public File createProjectDependenciesFile(String dependencyManager, File scmRootFolderFile) {

        String pmFilePath = getDependencyManagerFilePath(dependencyManager, scmRootFolderFile);
        if (!pmFilePath.equals("")) {

            //TODO: produce the licenses here


            return new File("");
        } else {
            return null;
        }
    }

    private String getDependencyManagerFilePath(String dependencyManager, File scmRootFolderFile) {


        try {
            if (dependencyManager.equals("NPM") || dependencyManager.equals("npm")) {
                return searchFile(scmRootFolderFile, "package.json").getAbsolutePath();
            } else if (dependencyManager.equals("MAVEN") || dependencyManager.equals("maven")) {
                return searchFile(scmRootFolderFile, "pom.xml").getAbsolutePath();
            } else if (dependencyManager.equals("GRADLE") || dependencyManager.equals("gradle")) {
                return searchFile(scmRootFolderFile, "init.gradle").getAbsolutePath();
            }
        } catch (NullPointerException e) {
            return "";
        }
        return "";
    }

    private static File searchFile(File folder, String fileName) {
        if (folder.isDirectory()) {
            File[] arr = folder.listFiles();
            for (File f : arr) {
                File found = searchFile(f, fileName);
                if (found != null)
                    return found;
            }
        } else {
            if (folder.getName().equals(fileName)) {
                return folder;
            }
        }
        return null;
    }


}
