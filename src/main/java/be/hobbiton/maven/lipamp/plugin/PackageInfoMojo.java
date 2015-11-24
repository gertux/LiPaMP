package be.hobbiton.maven.lipamp.plugin;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import be.hobbiton.maven.lipamp.common.ArchiveException;
import be.hobbiton.maven.lipamp.deb.DebInfo;

/**
 * Print information about a package
 *
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 * @since 1.2.0
 */
@Mojo(name = "info", requiresProject = false)
public class PackageInfoMojo extends AbstractMojo {
    /**
     * The package file
     */
    @Parameter(property = "file", required = true)
    private File file;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!this.file.exists()) {
            throw new MojoFailureException(this.file.getPath() + " does not exist");
        }
        try {
            DebInfo info = new DebInfo(this.file, getLog());
            getLog().info(info.toString());
        } catch (ArchiveException e) {
            throw new MojoFailureException("Failed to process " + this.file.getPath(), e);
        }
    }

    protected void setFile(File file) {
        this.file = file;
    }
}
