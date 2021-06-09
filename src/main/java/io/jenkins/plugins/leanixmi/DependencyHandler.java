package io.jenkins.plugins.leanixmi;

import hudson.model.TaskListener;
import jenkins.model.Jenkins;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DependencyHandler {


    private static final String NO_PERMISSION_TO_EXECUTE = "You have no permissions to execute a script file. Please contact your administrator!";
    private static final String WINDOWS = "Windows";
    private static final String GRADLE = "gradle";
    private final String OS = System.getProperty("os.name");



    public File createProjectDependenciesFile(String dependencyManager, File scmRootFolderFile, String scmRootFolder, TaskListener listener) {

        String dmFilePath = getDependencyManagerFilePath(dependencyManager, scmRootFolderFile, scmRootFolder);
        if (!dmFilePath.equals("")) {


            ProcessBuilder processBuilder = new ProcessBuilder();

            String filePath = "";
            String fileName = "";
            if (OS.contains(WINDOWS)) {
                fileName = "build_licenses.bat";
            } else {
                fileName = "build_licenses.sh";
            }
            filePath = Jenkins.get().getRootDir() + "/leanix/console_scripts/" + fileName;
            BufferedReader reader;
            try {

                File file = new File(filePath);
                if (file.exists()) {
                    if (!file.setExecutable(true)) {
                        throw new SecurityException(NO_PERMISSION_TO_EXECUTE);
                    }
                } else {
                    String scriptFileCopiedPath = copyFileFromWebappToLocal("/console_scripts/" + fileName, "/console_scripts/" + fileName);
                    File scriptFile = new File(scriptFileCopiedPath);
                    if (scriptFile.exists()) {
                        if (!file.setExecutable(true)) {
                            throw new SecurityException(NO_PERMISSION_TO_EXECUTE);
                        }
                    }
                }

                if (dependencyManager.equalsIgnoreCase(GRADLE)) {
                    String gradleInitFileName = "miCiCd-init.gradle";
                    String gradleInitFileLocalPath = Jenkins.get().getRootDir() + "/leanix/console_scripts/" + gradleInitFileName;

                    // copy the file from the webserver to the local directory if it doesn't exist yet
                    if (!new File(gradleInitFileLocalPath).exists()) {
                        copyFileFromWebappToLocal("/console_scripts/" + gradleInitFileName, "/console_scripts/" + gradleInitFileName);
                    }
                    if (!OS.contains(WINDOWS)) {
                        dmFilePath = dmFilePath + "/";
                    }
                    processBuilder.command(filePath, dmFilePath, dependencyManager, gradleInitFileLocalPath);

                } else {
                    if (!OS.contains(WINDOWS)) {
                        dmFilePath = dmFilePath + "/";
                    }
                    processBuilder.command(filePath, dmFilePath, dependencyManager);
                }


                processBuilder.redirectErrorStream(true);

                System.out.println("LeanIX Microservice Intelligence: Starting to build the dependencies file...");
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
                    System.out.println("LeanIX Microservice Intelligence: Success in building the dependencies file!");
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
                    } else if (dependencyManager.equalsIgnoreCase(GRADLE)) {
                        File depFile = new File(dmFilePath + "/build/reports/dependency-license/licenses.json");
                        if (depFile.exists()) {
                            return depFile;
                        }
                    }
                } else {
                    System.out.println("LeanIX Microservice Intelligence: ERROR in building the dependencies file!");
                }
                System.out.println(output);
            } catch (NullPointerException e) {
                System.out.println(e.getMessage());
                listener.getLogger().println(e.getMessage());
            } catch (IOException e) {
                listener.getLogger().println(e.getMessage());
            } catch (InterruptedException e) {
                listener.getLogger().println(e.getMessage());
            } catch (SecurityException e) {
                listener.getLogger().println(e.getMessage());
            }

            return null;
        } else {
            return null;
        }
    }

    private String getDependencyManagerFilePath(String dependencyManager, File scmRootFolderFile, String scmRootFolder) {


        try {
            if (dependencyManager.equalsIgnoreCase("npm")) {
                String npmPath = searchDependencyFile(scmRootFolder, scmRootFolderFile, "package.json", dependencyManager).getAbsolutePath();
                if (npmPath != null) {
                    return npmPath;
                }
            } else if (dependencyManager.equalsIgnoreCase("maven")) {
                String mavenPath = searchDependencyFile(scmRootFolder, scmRootFolderFile, "pom.xml", dependencyManager).getAbsolutePath();
                if (mavenPath != null) {
                    return mavenPath;
                }
            } else if (dependencyManager.equalsIgnoreCase(GRADLE)) {
                String gradlePath = searchDependencyFile(scmRootFolder, scmRootFolderFile, "build.gradle", dependencyManager).getAbsolutePath();
                if (gradlePath != null) {
                    return gradlePath;
                }
            }
        } catch (NullPointerException e) {
            return "";
        }
        return "";
    }

    private File searchDependencyFile(String scmRootFolder, File file, String fileName, String dependencyManager) {
        if (file.isDirectory()) {
            File[] arr = file.listFiles();
            if (arr != null) {
                for (File f : arr) {
                    boolean check = Paths.get(f.getAbsolutePath()).startsWith(scmRootFolder + "/app");

                    //deal with npm's node_modules here, otherwise all the package.json from there will be found after npm install
                    if (!dependencyManager.equalsIgnoreCase("npm") || (!f.getPath().contains("node_modules") && !check)) {
                        File found = searchDependencyFile(scmRootFolder, f, fileName, dependencyManager);
                        if (found != null) {
                            return found;
                        }
                    }
                }
            }
        } else {
            if (file.getName().equals(fileName)) {
                return new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - file.getName().length() - 1));
            } else {
                if (getFileEnding(file.getName()).equals(GRADLE)) {
                    return new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - file.getName().length() - 1));
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

    private String copyFileFromWebappToLocal(String relativeWebAppPath, String relativeLocalFilePath) throws IOException {
        String rootUrl = Jenkins.get().getRootUrl();
        String absoluteLocalFilePath = Jenkins.get().getRootDir() + "/leanix" + relativeLocalFilePath;
        if (rootUrl != null) {
            String fileURL = rootUrl.substring(0, rootUrl.length() - 1) + Jenkins.RESOURCE_PATH + "/plugin/leanix-microservice-intelligence" + relativeWebAppPath;
            InputStream in = null;
            try {
                in = new URL(fileURL).openStream();
                Path dirToCreate = Paths.get(absoluteLocalFilePath).getParent();
                if (dirToCreate != null) {
                    Files.createDirectories(dirToCreate);
                    Files.copy(in, Paths.get(absoluteLocalFilePath), StandardCopyOption.REPLACE_EXISTING);
                    return absoluteLocalFilePath;
                }
            } catch (IOException e) {
                throw e;
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
        throw new NullPointerException("Jenkins Root URL is empty, files in webapp can not be accessed. File: " + relativeWebAppPath);
    }
}
