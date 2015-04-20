package com.qiwi.gradle.dependencies.git;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.gradle.api.artifacts.Dependency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by nixan on 16.04.15.
 */
public class GitArtifactDependency implements Dependency {

    private final static String INLAY_REPOS_FOLDER = ".gitdependencies";

    private final String mURL;

    private final String mTarget;

    public GitArtifactDependency(String URL, String tagOrCommit)
            throws GitAPIException, IOException {
        mURL = URL;
        mTarget = tagOrCommit;

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException e) {

        }

        String projectName = FilenameUtils.getBaseName(URL);

        File reposFolder = new File(INLAY_REPOS_FOLDER + File.separator + projectName);
        if (!reposFolder.exists()) {
            reposFolder.mkdirs();
        }

        try {
            Git.open(reposFolder);
        } catch (RepositoryNotFoundException e) {
            System.out.println("Cloning remote repository: " + URL);
            Git.cloneRepository().setURI(URL).setDirectory(reposFolder).call();
        }
        Git.open(reposFolder).fetch().call();
        Git.open(reposFolder).checkout().setName(mTarget).call();

        createRootGradleScript();

        Process p;
        p = Runtime.getRuntime().exec("./gradlew --init-script ../dependencies.gradle clean assembleRelease",
                new String[]{"ANDROID_HOME=" + System.getProperty("sdk.dir"),
                        "JAVA_HOME=" + System.getProperty("java.home")}, reposFolder);
        printOutputs(p.getInputStream(), p.getErrorStream());
        p = Runtime.getRuntime().exec("./gradlew --init-script ../dependencies.gradle doNothing",
                new String[]{"ANDROID_HOME=" + System.getProperty("sdk.dir"),
                        "JAVA_HOME=" + System.getProperty("java.home")}, reposFolder);
        printOutputs(p.getInputStream(), p.getErrorStream());
    }

    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean contentEquals(Dependency dependency) {
        return false;
    }

    @Override
    public Dependency copy() {
        return null;
    }

    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
                String authType) {
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                String authType) {
        }
    }};

    private static class OutputStreamToSystemOut extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            System.out.write(b);
        }
    }

    private static void printOutputs(InputStream executionStream, InputStream errorStream)
            throws IOException {
        String executionLine;
        BufferedReader executionBufferedReader = new BufferedReader(
                new InputStreamReader(executionStream));
        while ((executionLine = executionBufferedReader.readLine()) != null) {
            System.out.println(executionLine);
        }
        executionBufferedReader.close();
        String errorLine;
        BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(errorStream));
        while ((errorLine = errorBufferedReader.readLine()) != null) {
            System.err.println(errorLine);
        }
        errorBufferedReader.close();
    }

    private static void createRootGradleScript() throws IOException {

        // Create root gradle buildfile
        File rootFolder = new File(INLAY_REPOS_FOLDER);
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }
        InputStream rootGradleScriptInputStream = GitArtifactDependency.class.getClassLoader()
                .getResourceAsStream("dependencies.gradle");
        FileOutputStream rootGradleScriptFileOutputStream = new FileOutputStream(
                INLAY_REPOS_FOLDER + File.separator + "dependencies.gradle");
        int readBytes;
        byte[] buffer = new byte[4096];
        while ((readBytes = rootGradleScriptInputStream.read(buffer)) > 0) {
            rootGradleScriptFileOutputStream.write(buffer, 0, readBytes);
        }
        rootGradleScriptInputStream.close();
        rootGradleScriptFileOutputStream.close();
    }
}
