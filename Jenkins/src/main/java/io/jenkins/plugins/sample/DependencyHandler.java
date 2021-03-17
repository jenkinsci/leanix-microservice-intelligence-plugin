package io.jenkins.plugins.sample;

import jenkins.model.Jenkins;

import java.io.*;

public class DependencyHandler {

    private final String OS = System.getProperty("os.name");

    public File createProjectDependenciesFile(String dependencyManager, File scmRootFolderFile) {

        String dmFilePath = getDependencyManagerFilePath(dependencyManager, scmRootFolderFile);
        if (!dmFilePath.equals("")) {


            if (OS.contains("Windows")) {

                // TODO: the console-script must be placed correctly in the work-dir at first start of Jenkins or gotten from the resources webserver-folder

                ProcessBuilder processBuilder = new ProcessBuilder(Jenkins.get().getRootDir() + "\\leanix\\console_scripts\\build_licenses.bat", dmFilePath, dependencyManager);
                try {

                    processBuilder.redirectErrorStream(true);

                    Process process = processBuilder.start();

                    StringBuilder output = new StringBuilder();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line + "\n");
                    }

                    int exitVal = process.waitFor();
                    if (exitVal == 0) {
                        System.out.println("Success!");

                    } else {
                        System.out.println("ERROR!");
                    }
                    System.out.println(output);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                //TODO: produce the licenses for Linux here
            }


            return new File("");
        } else {
            return null;
        }
    }

    private String getDependencyManagerFilePath(String dependencyManager, File scmRootFolderFile) {


        try {
            if (dependencyManager.equals("NPM") || dependencyManager.equals("npm")) {
                String npmPath = searchDependencyFile(scmRootFolderFile, "package.json", dependencyManager).getAbsolutePath();
                return npmPath.substring(0, npmPath.length() - "package.json".length() - 1);
            } else if (dependencyManager.equals("MAVEN") || dependencyManager.equals("maven")) {
                String mavenPath = searchDependencyFile(scmRootFolderFile, "pom.xml", dependencyManager).getAbsolutePath();
                return mavenPath.substring(0, mavenPath.length() - "pom.xml".length() - 1);
            } else if (dependencyManager.equals("GRADLE") || dependencyManager.equals("gradle")) {
                return searchDependencyFile(scmRootFolderFile, "", dependencyManager).getAbsolutePath();
            }
        } catch (NullPointerException e) {
            return "";
        }
        return "";
    }

    private File searchDependencyFile(File file, String fileName, String dependencyManager) {
        if (file.isDirectory()) {
            File[] arr = file.listFiles();
            for (File f : arr) {
                //deal with npm's node_modules here, otherwise all the package.json from there will be found after npm install
                if (!(dependencyManager.equals("NPM") || dependencyManager.equals("npm")) || !f.getPath().contains("node_modules")) {
                    File found = searchDependencyFile(f, fileName, dependencyManager);
                    if (found != null)
                        return found;
                }
            }
        } else {
            if (!dependencyManager.equals("GRADLE") || !dependencyManager.equals("gradle")) {
                if (file.getName().equals(fileName)) {
                    return file;
                }
            } else {
                if (getFileEnding(file.getName()).equals("gradle")) {
                    File gradleFolder = new File(file.getAbsolutePath().substring(0, file.getName().length() - 1));
                    return gradleFolder;
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
