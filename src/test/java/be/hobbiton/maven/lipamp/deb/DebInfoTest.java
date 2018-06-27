package be.hobbiton.maven.lipamp.deb;

import be.hobbiton.maven.lipamp.common.LinuxPackagingException;
import be.hobbiton.maven.lipamp.common.Slf4jLogImpl;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static be.hobbiton.maven.lipamp.common.TestConstants.*;
import static be.hobbiton.maven.lipamp.deb.DebInfo.DebianInfoFile.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DebInfoTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebInfoTest.class);
    private static final Log PLUGIN_LOGGER = new Slf4jLogImpl();
    private static final Path TEST_DATA_DIR = Paths.get("src/test/data");
    private static final Path PACKAGE_FILE_PATH = TEST_DATA_DIR.resolve("hiapp-pkg-1.0.0.deb");
    private static final File PACKAGE_FILE = PACKAGE_FILE_PATH.toFile();

    private static void assertPackage(DebInfo debInfo) {
        assertNotNull(debInfo);
        LOGGER.debug(debInfo.toString());
        assertControl(debInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertConffiles(debInfo, new String[]{"/etc/hiapp/hiapp.conf"});
        assertEquals(8, debInfo.getDataFiles().size());
        assertEquals(FILE_PACKAGENAME, debInfo.getControl().getPackageName());
        assertEquals(FILE_SECTION, debInfo.getControl().getSection());
        assertEquals(FILE_PRIORITY, debInfo.getControl().getPriority());
        assertEquals(FILE_MAINTAINER, debInfo.getControl().getMaintainer());
        assertEquals(FILE_INSTALLED_SIZE, debInfo.getControl().getInstalledSize());
        assertEquals(FILE_VERSION, debInfo.getControl().getVersion());
        assertEquals(FILE_DESCR_SYNOPSIS, debInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debInfo.getControl().getDescription());
        assertEquals(FILE_DEPENDS, debInfo.getControl().getDepends());
        assertEquals(FILE_ARCHITECTURE, debInfo.getControl().getArchitecture());
    }

    @Test
    public void testGetInfoPath() throws LinuxPackagingException {
        assertPackage(new DebInfo(PACKAGE_FILE_PATH, PLUGIN_LOGGER));
    }

    @Test
    public void testGetInfoFile() throws LinuxPackagingException {
        assertPackage(new DebInfo(PACKAGE_FILE, PLUGIN_LOGGER));
    }

    @Test(expected = LinuxPackagingException.class)
    public void testNoDebianBinary() throws LinuxPackagingException {
        new DebInfo(TEST_DATA_DIR.resolve("hiapp-pkg-no_debian_binary.deb"), PLUGIN_LOGGER);
    }

    @Test(expected = LinuxPackagingException.class)
    public void testNoControl() throws LinuxPackagingException {
        new DebInfo(TEST_DATA_DIR.resolve("hiapp-pkg-no_control.deb"), PLUGIN_LOGGER);
    }

    @Test(expected = LinuxPackagingException.class)
    public void testNoData() throws LinuxPackagingException {
        new DebInfo(TEST_DATA_DIR.resolve("hiapp-pkg-no_data.deb"), PLUGIN_LOGGER);
    }

}
