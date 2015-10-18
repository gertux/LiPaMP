package be.hobbiton.maven.lipamp.deb;

import static be.hobbiton.maven.lipamp.common.TestConstants.*;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebInfoTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebInfoTest.class);
    private File packageFile = new File("src/test/data/hiapp-pkg-1.0.0.deb");

    @Test
    public void testGetInfo() throws DebianArchiveException {
        DebInfo debInfo = new DebInfo(this.packageFile);
        LOGGER.debug(debInfo.toString());
        assertEquals(3, debInfo.getControlFiles().size());
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
}
