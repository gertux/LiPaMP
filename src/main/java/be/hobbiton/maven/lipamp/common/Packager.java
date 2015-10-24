package be.hobbiton.maven.lipamp.common;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

public interface Packager {
    void write(File outputFile) throws MojoExecutionException;
}
