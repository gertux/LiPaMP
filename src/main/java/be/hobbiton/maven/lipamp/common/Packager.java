package be.hobbiton.maven.lipamp.common;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

public interface Packager {
    void write(File outputFile) throws MojoExecutionException;
}
