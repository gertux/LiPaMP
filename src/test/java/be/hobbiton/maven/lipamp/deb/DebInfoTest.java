package be.hobbiton.maven.lipamp.deb;

import static be.hobbiton.maven.lipamp.common.TestConstants.*;
import static be.hobbiton.maven.lipamp.deb.DebInfo.DebianInfoFile.*;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.hobbiton.maven.lipamp.common.Slf4jLogImpl;

public class DebInfoTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebInfoTest.class);
    private static final Log PLUGIN_LOGGER = new Slf4jLogImpl();
    private static final Path PACKAGE_FILE_PATH = Paths.get("src/test/data/hiapp-pkg-1.0.0.deb");
    private static final File PACKAGE_FILE = PACKAGE_FILE_PATH.toFile();

    @Test
    public void testGetInfoPath() throws DebianArchiveException {
        DebInfo debInfo = new DebInfo(PACKAGE_FILE_PATH, PLUGIN_LOGGER);
        assertNotNull(debInfo);
        LOGGER.debug(debInfo.toString());
        DebInfoTest.assertControl(debInfo,
                new String[] { CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename() });
        DebInfoTest.assertConffiles(debInfo, new String[] { "/etc/hiapp/hiapp.conf" });
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
    public void testGetInfoFile() throws DebianArchiveException {
        DebInfo debInfo = new DebInfo(PACKAGE_FILE, PLUGIN_LOGGER);
        LOGGER.debug(debInfo.toString());
        DebInfoTest.assertControl(debInfo,
                new String[] { CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename() });
        DebInfoTest.assertConffiles(debInfo, new String[] { "/etc/hiapp/hiapp.conf" });
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

    public static void assertConffiles(DebInfo debianInfo, String[] filenames) {
        Set<String> notFound = new HashSet<>(Arrays.asList(filenames));
        Collection<File> conffiles = debianInfo.getConffiles();
        if (conffiles != null) {
            for (File configFile : conffiles) {
                String conffilePath = configFile.getAbsolutePath();
                if (notFound.contains(conffilePath)) {
                    notFound.remove(conffilePath);
                } else {
                    fail("found unexpected file: " + conffilePath);
                }
            }
        }
        assertEquals("Missing: " + notFound, 0, notFound.size());
    }

    public static void assertControl(DebInfo debianInfo, String[] filenames) {
        Set<String> notFound = new HashSet<String>(Arrays.asList(filenames));
        for (File controlFile : debianInfo.getControlFiles()) {
            if (notFound.contains(controlFile.getName())) {
                notFound.remove(controlFile.getName());
            } else {
                fail("found unexpected file: " + controlFile.getName());
            }
        }
        assertEquals("Missing: " + notFound, 0, notFound.size());
    }
}
