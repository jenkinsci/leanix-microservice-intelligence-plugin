package io.jenkins.plugins.leanixmi;

import jenkins.model.Jenkins;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DependencyHandler {

    private final String OS = System.getProperty("os.name");

    public File createProjectDependenciesFile(String dependencyManager, File scmRootFolderFile) {

        String dmFilePath = getDependencyManagerFilePath(dependencyManager, scmRootFolderFile);
        if (!dmFilePath.equals("")) {


            ProcessBuilder processBuilder = new ProcessBuilder();

            // TODO: the console-script could be moved to a String variable in a different class or sth., then the path stuff will not be needed any longer

            String filePath = "";
            if (OS.contains("Windows")) {
                filePath = Jenkins.get().getRootDir() + "/leanix/console_scripts/build_licenses.bat";
            } else {
                filePath = Jenkins.get().getRootDir() + "/leanix/console_scripts/build_licenses.sh";
            }
            BufferedReader reader;
            try {

                File file = new File(filePath);
                if (file.exists()) {
                    file.setExecutable(true);
                } else {
                    return null;
                }

                if (dependencyManager.equalsIgnoreCase("gradle")) {
                    String gradleInitFileName = "miCiCd-init.gradle";
                    String gradleInitFileLocalPath = Jenkins.get().getRootDir() + "/leanix/console_scripts/" + gradleInitFileName;
                    String url = Jenkins.get().getRootUrl();
                    // copy the file from the webserver to the local directory if it doesn't exist yet
                    if (url != null && !new File(gradleInitFileLocalPath).exists()) {
                        String gradleScriptUrl = url.substring(0, url.length() - 1) + Jenkins.RESOURCE_PATH + "/plugin/leanix-microservice-intelligence/console_scripts/" + gradleInitFileName;
                        InputStream in = new URL(gradleScriptUrl).openStream();
                        Files.copy(in, Paths.get(gradleInitFileLocalPath), StandardCopyOption.REPLACE_EXISTING);
                    }
                    processBuilder.command(filePath, dmFilePath, dependencyManager, gradleInitFileLocalPath);

                } else {
                    processBuilder.command(filePath, dmFilePath, dependencyManager);
                }

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
                    } else if (dependencyManager.equalsIgnoreCase("gradle")) {
                        File depFile = new File(dmFilePath + "/build/reports/dependency-license/licenses.json");
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
                    File gradleFolder = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - file.getName().length() - 1));
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
