package com.qiwi.gradle.dependencies.git;

import org.gradle.api.artifacts.Dependency;

/**
 * Created by nixan on 16.04.15.
 */
public class GitArtifactDependency implements Dependency {

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
    
}
