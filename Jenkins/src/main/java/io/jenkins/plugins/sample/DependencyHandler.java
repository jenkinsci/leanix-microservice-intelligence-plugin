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
                return searchFile(scmRootFolderFile, "package.json", dependencyManager).getAbsolutePath();
            } else if (dependencyManager.equals("MAVEN") || dependencyManager.equals("maven")) {
                return searchFile(scmRootFolderFile, "pom.xml", dependencyManager).getAbsolutePath();
            } else if (dependencyManager.equals("GRADLE") || dependencyManager.equals("gradle")) {
                return searchFile(scmRootFolderFile, "init.gradle", dependencyManager).getAbsolutePath();
            }
        } catch (NullPointerException e) {
            return "";
        }
        return "";
    }

    private File searchFile(File file, String fileName, String dependencyManager) {
        if (file.isDirectory()) {
            File[] arr = file.listFiles();
            for (File f : arr) {
                File found = searchFile(f, fileName, dependencyManager);
                if (found != null)
                    return found;
            }
        } else {
            if(!dependencyManager.equals("GRADLE") || !dependencyManager.equals("gradle") ) {
                if (file.getName().equals(fileName)) {
                    return file;
                }
            }else{
                if(getFileEnding(file.getName()).equals("gradle")){
                    return file;
                }
            }
        }
        return null;
    }

    private String getFileEnding(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        }
        return "";
    }
}
