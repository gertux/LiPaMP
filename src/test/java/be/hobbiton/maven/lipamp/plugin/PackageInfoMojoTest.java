package be.hobbiton.maven.lipamp.plugin;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import be.hobbiton.maven.lipamp.common.Slf4jLogImpl;

public class PackageInfoMojoTest {
    private static final Log PLUGIN_LOGGER = new Slf4jLogImpl();
    private File debianPackageFile = new File("src/test/data/hiapp-pkg-1.0.0.deb");
    private File unknownFileType = new File("src/test/data/hiapp-1.0.0.txt");
    private File nonexistingFile = new File("src/test/data/hiapp-1.0.0.deb");
    private PackageInfoMojo mojo;

    @Before
    public void setUp() throws Exception {
        assertTrue(this.debianPackageFile.isFile());
        assertFalse(this.nonexistingFile.isFile());
        this.mojo = new PackageInfoMojo();
        this.mojo.setLog(PLUGIN_LOGGER);
    }

    @Test(expected = MojoExecutionException.class)
    public void testExecuteFileNotFound() throws Exception {
        this.mojo.setFile(this.nonexistingFile);
        this.mojo.execute();
    }

    @Test(expected = MojoExecutionException.class)
    public void testExecuteUnknownFiletype() throws Exception {
        this.mojo.setFile(this.unknownFileType);
        this.mojo.execute();
    }

    @Test
    public void testExecute() throws Exception {
        this.mojo.setFile(this.debianPackageFile);
        this.mojo.execute();
    }
}
