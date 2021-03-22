package io.jenkins.plugins.leanixmi;

import jenkins.model.Jenkins;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class DependencyHandler {

    private final String OS = System.getProperty("os.name");

    public File createProjectDependenciesFile(String dependencyManager, File scmRootFolderFile) {

        String dmFilePath = getDependencyManagerFilePath(dependencyManager, scmRootFolderFile);
        if (!dmFilePath.equals("")) {


            ProcessBuilder processBuilder = new ProcessBuilder();

            String filePath = "";

            // TODO: the console-script could be moved to a String variable in a different class or sth. like that
            if (OS.contains("Windows")) {
                filePath = Jenkins.RESOURCE_PATH + "/plugin/leanix-microservice-intelligence/console_scripts/build_licenses.bat";
            } else {
                filePath = Jenkins.RESOURCE_PATH + "/plugin/leanix-microservice-intelligence/console_scripts/build_licenses.sh";
            }
            BufferedReader reader;
            try {

                File file = new File(filePath);
                if (file.exists()) {
                    file.setExecutable(true);
                }else{
                    return null;
                }

                processBuilder.command(filePath, dmFilePath, dependencyManager);
                // processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.redirectErrorStream(true);

                Process process = processBuilder.start();

                StringBuilder output = new StringBuilder();

                reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line + "\n");
                }
                reader.close();
                int exitVal = process.waitFor();
                if (exitVal == 0) {
                    System.out.println("Success!");
                    if (dependencyManager.equalsIgnoreCase("npm")) {
                        File depFile = new File(dmFilePath + "/dependencies.json");
                        if (depFile.exists()) {
                            return depFile;
                        }
                    } else if (dependencyManager.equalsIgnoreCase("maven")) {
                        File depFile = new File(dmFilePath + "/target/generated-resources/licenses.xml");
                        if (depFile.exists()) {
                            return depFile;
                        }
                    }
                } else {
                    System.out.println("ERROR!");
                }
                System.out.println(output);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        } else {
            return null;
        }
    }

    private String getDependencyManagerFilePath(String dependencyManager, File scmRootFolderFile) {


        try {
            if (dependencyManager.equalsIgnoreCase("npm")) {
                String npmPath = searchDependencyFile(scmRootFolderFile, "package.json", dependencyManager).getAbsolutePath();
                if (npmPath != null) {
                    return npmPath.substring(0, npmPath.length() - "package.json".length() - 1);
                }
            } else if (dependencyManager.equalsIgnoreCase("maven")) {
                String mavenPath = searchDependencyFile(scmRootFolderFile, "pom.xml", dependencyManager).getAbsolutePath();
                if (mavenPath != null) {
                    return mavenPath.substring(0, mavenPath.length() - "pom.xml".length() - 1);
                }
            } else if (dependencyManager.equalsIgnoreCase("gradle")) {
                String gradlePath = searchDependencyFile(scmRootFolderFile, "init.gradle", dependencyManager).getAbsolutePath();
                if (gradlePath != null) {
                    return gradlePath;
                }
            }
        } catch (NullPointerException e) {
            return "";
        }
        return "";
    }

    private File searchDependencyFile(File file, String fileName, String dependencyManager) {
        if (file.isDirectory()) {
            File[] arr = file.listFiles();
            if (arr != null) {
                for (File f : arr) {
                    //deal with npm's node_modules here, otherwise all the package.json from there will be found after npm install
                    if (!dependencyManager.equalsIgnoreCase("npm") || !f.getPath().contains("node_modules")) {
                        File found = searchDependencyFile(f, fileName, dependencyManager);
                        if (found != null)
                            return found;
                    }
                }
            }
        } else {
            if (file.getName().equals(fileName)) {
                return file;
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
