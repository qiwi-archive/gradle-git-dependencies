package com.qiwi.gradle.dependencies.git

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler

class GitDependency implements Plugin<Project> {

    void apply(Project target) {
        target.repositories {
            mavenLocal()
        }
        target.dependencies.metaClass.mixin(GitDependencyExtension)
    }
}

class GitDependencyExtension {

    public static Dependency git(DependencyHandler dependencyHandler, String url, String branch,
            String tag) {
        return new GitArtifactDependency(url, branch, tag);
    }
}