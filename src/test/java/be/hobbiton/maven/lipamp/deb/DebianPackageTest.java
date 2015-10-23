package be.hobbiton.maven.lipamp.deb;

import static be.hobbiton.maven.lipamp.common.TestConstants.*;
import static be.hobbiton.maven.lipamp.deb.DebInfo.DebianInfoFile.*;
import static be.hobbiton.maven.lipamp.deb.DebianPackage.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.hobbiton.maven.lipamp.common.ArchiveEntry;
import be.hobbiton.maven.lipamp.common.DirectoryArchiveEntry;
import be.hobbiton.maven.lipamp.common.FileArchiveEntry;

public class DebianPackageTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebianPackageTest.class);
    private static final String ROOT_GROUPNAME = "sys";
    private static final String ROOT_USERNAME = "root";
    private static final List<File> CONTROL_FILES = new ArrayList<File>();
    private static final List<ArchiveEntry> DATA_FILES = new ArrayList<ArchiveEntry>();
    private File outputFile;

    static {
        CONTROL_FILES.add(new File(CONF_BASEDIR, CONFFILES.getFilename()));
        CONTROL_FILES.add(new File(CONF_BASEDIR, CONTROL.getFilename()));
        CONTROL_FILES.add(new File(CONF_BASEDIR, POST_INSTALL.getFilename()));
        DATA_FILES.add(new DirectoryArchiveEntry("./etc", ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_DIR_MODE));
        DATA_FILES.add(new DirectoryArchiveEntry("./etc/hiapp/", ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_DIR_MODE));
        DATA_FILES.add(new FileArchiveEntry("./etc/hiapp/hiapp.conf", new File(BASEDIR, "deb/etc/hiapp/hiapp.conf"),
                ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_FILE_MODE));
        DATA_FILES.add(new DirectoryArchiveEntry("./etc/init/", ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_DIR_MODE));
        DATA_FILES.add(new FileArchiveEntry("./etc/init/hiapp.conf", new File(BASEDIR, "deb/etc/init/hiapp.conf"),
                ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_FILE_MODE));
        DATA_FILES.add(new DirectoryArchiveEntry("./opt/", ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_DIR_MODE));
        DATA_FILES.add(new DirectoryArchiveEntry("./opt/hiapp/", ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_DIR_MODE));
        DATA_FILES.add(new FileArchiveEntry("./opt/hiapp/hiapp.jar", new File("src/test/data/hiapp-1.0.0.jar"),
                ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_FILE_MODE));
        DATA_FILES.add(new DirectoryArchiveEntry("./var/", ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_DIR_MODE));
        DATA_FILES.add(new DirectoryArchiveEntry("./var/log/", ROOT_USERNAME, ROOT_GROUPNAME, DEFAULT_DIR_MODE));
        DATA_FILES.add(new DirectoryArchiveEntry("./var/log/hiapp/", "hiapp", "wheel", Integer.parseInt("775", 8)));
    }

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

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyControl() throws Exception {
        new DebianPackage(Collections.<File> emptyList(), DATA_FILES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullControl() throws Exception {
        new DebianPackage(null, DATA_FILES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyData() throws Exception {
        new DebianPackage(CONTROL_FILES, Collections.<ArchiveEntry> emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullData() throws Exception {
        new DebianPackage(CONTROL_FILES, null);
    }

    @Test
    public void testFromFiles() throws Exception {
        DebianPackage debPackage = new DebianPackage(CONTROL_FILES, DATA_FILES);
        debPackage.write(this.outputFile);
        DebInfo debianInfo = new DebInfo(this.outputFile);
        LOGGER.debug(debianInfo.toString());
        assertEquals(3, debianInfo.getControlFiles().size());
        assertEquals(11, debianInfo.getDataFiles().size());
    }
}
