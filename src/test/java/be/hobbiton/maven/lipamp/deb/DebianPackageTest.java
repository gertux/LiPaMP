package be.hobbiton.maven.lipamp.deb;

import be.hobbiton.maven.lipamp.common.ArchiveEntry;
import be.hobbiton.maven.lipamp.common.DirectoryArchiveEntry;
import be.hobbiton.maven.lipamp.common.FileArchiveEntry;
import be.hobbiton.maven.lipamp.common.Slf4jLogImpl;
import org.apache.maven.plugin.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static be.hobbiton.maven.lipamp.common.TestConstants.*;
import static be.hobbiton.maven.lipamp.deb.DebInfo.DebianInfoFile.*;
import static be.hobbiton.maven.lipamp.deb.DebianPackage.DEFAULT_DIR_MODE;
import static be.hobbiton.maven.lipamp.deb.DebianPackage.DEFAULT_FILE_MODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DebianPackageTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebianPackageTest.class);
    private static final String ROOT_GROUPNAME = "sys";
    private static final String ROOT_USERNAME = "root";
    private static final List<File> CONTROL_FILES = new ArrayList<File>();
    private static final List<Path> CONTROL_PATHS = Stream.of(CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()).map
            (CONF_BASEPATH::resolve).collect(Collectors.toList());
    private static final List<ArchiveEntry> DATA_FILES = new ArrayList<ArchiveEntry>();
    private static final Log PLUGIN_LOGGER = new Slf4jLogImpl();

    static {
        CONTROL_FILES.add(new File(CONF_BASEDIR, CONFFILES.getFilename()));
        CONTROL_FILES.add(new File(CONF_BASEDIR, CONTROL.getFilename()));
        CONTROL_FILES.add(new File(CONF_BASEDIR, POST_INSTALL.getFilename()));
        DATA_FILES.add(new DirectoryArchiveEntry("./etc", ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_DIR_MODE));
        DATA_FILES.add(new DirectoryArchiveEntry("./etc/hiapp/", ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_DIR_MODE));
        DATA_FILES.add(new FileArchiveEntry("./etc/hiapp/hiapp.conf", new File(BASEDIR, "deb/etc/hiapp/hiapp.conf"), ROOT_USERNAME, ROOT_GROUPNAME,
                DEFAULT_FILE_MODE));
        DATA_FILES.add(new DirectoryArchiveEntry("./etc/init/", ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_DIR_MODE));
        DATA_FILES.add(new FileArchiveEntry("./etc/init/hiapp.conf", new File(BASEDIR, "deb/etc/init/hiapp.conf"), ROOT_USERNAME, ROOT_GROUPNAME,
                DEFAULT_FILE_MODE));
        DATA_FILES.add(new DirectoryArchiveEntry("./opt/", ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_DIR_MODE));
        DATA_FILES.add(new DirectoryArchiveEntry("./opt/hiapp/", ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_DIR_MODE));
        DATA_FILES.add(new FileArchiveEntry("./opt/hiapp/hiapp.jar", new File("src/test/data/hiapp-1.0.0.jar"), ROOT_USERNAME, ROOT_GROUPNAME,
                DEFAULT_FILE_MODE));
        DATA_FILES.add(new DirectoryArchiveEntry("./var/", ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_DIR_MODE));
        DATA_FILES.add(new DirectoryArchiveEntry("./var/log/", ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_DIR_MODE));
        DATA_FILES.add(new DirectoryArchiveEntry("./var/log/hiapp/", "hiapp", "wheel", Integer.parseInt("775", 8)));
    }

    private File outputFile;

    @Before
    public void setUp() throws Exception {
        this.outputFile = new File("target/hiapp-pkg.deb");
        tearDown();
    }

    @After
    public void tearDown() throws Exception {
        if (this.outputFile != null && this.outputFile.exists()) {
            assertTrue(this.outputFile.delete());
        }
    }


    @Test
    public void testFromPaths() throws Exception {
        DebianPackage debPackage = new DebianPackage(PLUGIN_LOGGER, CONTROL_PATHS, DATA_FILES);
        debPackage.write(this.outputFile);
        assertPackage(this.outputFile);
    }

    @Test(expected = DebianPackage.DebianPackageException.class)
    public void testNoOutPutFile() {
        File parentDir = new File("NoSuchDir");
        assertFalse(parentDir.isDirectory());
        File output = new File(parentDir, "hiapp-pkg.deb");
        DebianPackage debPackage = new DebianPackage(PLUGIN_LOGGER, CONTROL_PATHS, DATA_FILES);
        debPackage.write(output);
        assertPackage(output);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyControlPaths() throws Exception {
        new DebianPackage(PLUGIN_LOGGER, Collections.<Path>emptyList(), DATA_FILES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullControlPaths() throws Exception {
        new DebianPackage(PLUGIN_LOGGER, null, DATA_FILES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyDataArchiveEntries() throws Exception {
        new DebianPackage(PLUGIN_LOGGER, CONTROL_PATHS, Collections.<ArchiveEntry>emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullDataArchiveEntries() throws Exception {
        new DebianPackage(PLUGIN_LOGGER, CONTROL_PATHS, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyControl() throws Exception {
        new DebianPackage(Collections.<File>emptyList(), DATA_FILES, PLUGIN_LOGGER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullControl() throws Exception {
        new DebianPackage(null, DATA_FILES, PLUGIN_LOGGER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyData() throws Exception {
        new DebianPackage(CONTROL_FILES, Collections.<ArchiveEntry>emptyList(), PLUGIN_LOGGER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullData() throws Exception {
        new DebianPackage(CONTROL_FILES, null, PLUGIN_LOGGER);
    }

    @Test
    public void testFromFiles() throws Exception {
        DebianPackage debPackage = new DebianPackage(CONTROL_FILES, DATA_FILES, PLUGIN_LOGGER);
        debPackage.write(this.outputFile);
        assertPackage(this.outputFile);
    }

    private void assertPackage(File output) {
        DebInfo debianInfo = new DebInfo(output, PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertEquals(3, debianInfo.getControlFiles().size());
        assertEquals(11, debianInfo.getDataFiles().size());
    }
}
