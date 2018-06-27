package be.hobbiton.maven.lipamp.deb;

import static be.hobbiton.maven.lipamp.common.Constants.INVALID_SIZE;
import static be.hobbiton.maven.lipamp.common.TestConstants.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;

import be.hobbiton.maven.lipamp.common.LinuxPackagingException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import be.hobbiton.maven.lipamp.common.Slf4jLogImpl;

public class DebianControlTest {
    private static final File OUTPUT_DIR = new File("target/DebianControlTest");
    private static final File NEW_CONTROL_FILE = new File(OUTPUT_DIR, DebInfo.DebianInfoFile.CONTROL.getFilename());
    private static final String HOMEPAGE = "http://www.home.com";
    private static final Log PLUGIN_LOGGER = new Slf4jLogImpl();

    @Before
    public void setUp() throws Exception {
        tearDown();
    }

    @After
    public void tearDown() throws Exception {
        if (OUTPUT_DIR.isDirectory()) {
            FileUtils.cleanDirectory(OUTPUT_DIR);
            assertTrue(OUTPUT_DIR.delete());
        }
    }

    @Test
    public void testFromFile() throws Exception {
        DebianControl control = new DebianControl(CONTROL_FILE, PLUGIN_LOGGER);
        assertEquals(FILE_PACKAGENAME, control.getPackageName());
        assertEquals(FILE_SECTION, control.getSection());
        assertEquals(FILE_PRIORITY, control.getPriority());
        assertEquals(FILE_MAINTAINER, control.getMaintainer());
        assertEquals(FILE_INSTALLED_SIZE, control.getInstalledSize());
        assertEquals(FILE_VERSION, control.getVersion());
        assertEquals(FILE_DESCR_SYNOPSIS, control.getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, control.getDescription());
        assertEquals(FILE_DEPENDS, control.getDepends());
        assertEquals(FILE_ARCHITECTURE, control.getArchitecture());
    }

    @Test
    public void testOutput() throws Exception {
        assertTrue(OUTPUT_DIR.mkdirs());
        DebianControl debianControl = new DebianControl();
        debianControl.setPackageName(FILE_PACKAGENAME);
        debianControl.setVersion(FILE_VERSION);
        debianControl.setArchitecture(FILE_ARCHITECTURE);
        debianControl.setMaintainer(FILE_MAINTAINER);
        debianControl.setDescriptionSynopsis(FILE_DESCR_SYNOPSIS);
        debianControl.setDescription(FILE_DESCRIPTION);
        debianControl.setHomepage(HOMEPAGE);
        FileOutputStream fos = new FileOutputStream(NEW_CONTROL_FILE);
        debianControl.write(fos);
        fos.close();
        DebianControl control = new DebianControl(NEW_CONTROL_FILE, PLUGIN_LOGGER);
        assertEquals(FILE_PACKAGENAME, control.getPackageName());
        assertNull(control.getSection());
        assertNull(control.getPriority());
        assertEquals(FILE_MAINTAINER, control.getMaintainer());
        assertEquals(INVALID_SIZE, control.getInstalledSize());
        assertEquals(FILE_VERSION, control.getVersion());
        assertEquals(FILE_DESCR_SYNOPSIS, control.getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, control.getDescription());
        assertNull(control.getDepends());
        assertEquals(FILE_ARCHITECTURE, control.getArchitecture());
        assertEquals(HOMEPAGE, control.getHomepage());
    }

    @Test
    public void testOutputAll() throws Exception {
        assertTrue(OUTPUT_DIR.mkdirs());
        DebianControl debianControl = new DebianControl();
        debianControl.setPackageName(FILE_PACKAGENAME);
        debianControl.setSection(FILE_SECTION);
        debianControl.setPriority(FILE_PRIORITY);
        debianControl.setVersion(FILE_VERSION);
        debianControl.setArchitecture(FILE_ARCHITECTURE);
        debianControl.setMaintainer(FILE_MAINTAINER);
        debianControl.setInstalledSize(FILE_INSTALLED_SIZE);
        debianControl.setDescriptionSynopsis(FILE_DESCR_SYNOPSIS);
        debianControl.setDescription(FILE_DESCRIPTION);
        debianControl.setDepends(FILE_DEPENDS);
        FileOutputStream fos = new FileOutputStream(NEW_CONTROL_FILE);
        debianControl.write(fos);
        fos.close();
        DebianControl control = new DebianControl(NEW_CONTROL_FILE, PLUGIN_LOGGER);
        assertEquals(FILE_PACKAGENAME, control.getPackageName());
        assertEquals(FILE_SECTION, control.getSection());
        assertEquals(FILE_PRIORITY, control.getPriority());
        assertEquals(FILE_MAINTAINER, control.getMaintainer());
        assertEquals(FILE_INSTALLED_SIZE, control.getInstalledSize());
        assertEquals(FILE_VERSION, control.getVersion());
        assertEquals(FILE_DESCR_SYNOPSIS, control.getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, control.getDescription());
        assertEquals(FILE_DEPENDS, control.getDepends());
        assertEquals(FILE_ARCHITECTURE, control.getArchitecture());
    }

    @Test(expected = LinuxPackagingException.class)
    public void testOutputInvalid() throws Exception {
        assertTrue(OUTPUT_DIR.mkdirs());
        DebianControl debianControl = new DebianControl();
        debianControl.setPackageName(FILE_PACKAGENAME);
        debianControl.setArchitecture(FILE_ARCHITECTURE);
        debianControl.setMaintainer(FILE_MAINTAINER);
        debianControl.setDescriptionSynopsis(FILE_DESCR_SYNOPSIS);
        debianControl.setDescription(FILE_DESCRIPTION);
        FileOutputStream fos = new FileOutputStream(NEW_CONTROL_FILE);
        debianControl.write(fos);
    }

    @Test
    public void testIsValidMinimum() {
        DebianControl debianControl = new DebianControl();
        debianControl.setPackageName(FILE_PACKAGENAME);
        debianControl.setVersion(FILE_VERSION);
        debianControl.setArchitecture(FILE_ARCHITECTURE);
        debianControl.setMaintainer(FILE_MAINTAINER);
        debianControl.setDescriptionSynopsis(FILE_DESCR_SYNOPSIS);
        debianControl.setDescription(FILE_DESCRIPTION);
        assertTrue(debianControl.isValid());
    }

    @Test
    public void testIsValidPackageNameMissing() {
        DebianControl debianControl = new DebianControl();
        debianControl.setVersion(FILE_VERSION);
        debianControl.setArchitecture(FILE_ARCHITECTURE);
        debianControl.setMaintainer(FILE_MAINTAINER);
        debianControl.setDescriptionSynopsis(FILE_DESCR_SYNOPSIS);
        debianControl.setDescription(FILE_DESCRIPTION);
        assertFalse(debianControl.isValid());
    }

    @Test
    public void testIsValidVersionMissing() {
        DebianControl debianControl = new DebianControl();
        debianControl.setPackageName(FILE_PACKAGENAME);
        debianControl.setArchitecture(FILE_ARCHITECTURE);
        debianControl.setMaintainer(FILE_MAINTAINER);
        debianControl.setDescriptionSynopsis(FILE_DESCR_SYNOPSIS);
        debianControl.setDescription(FILE_DESCRIPTION);
        assertFalse(debianControl.isValid());
    }

    @Test
    public void testIsValidArchMmissing() {
        DebianControl debianControl = new DebianControl();
        debianControl.setPackageName(FILE_PACKAGENAME);
        debianControl.setVersion(FILE_VERSION);
        debianControl.setMaintainer(FILE_MAINTAINER);
        debianControl.setDescriptionSynopsis(FILE_DESCR_SYNOPSIS);
        debianControl.setDescription(FILE_DESCRIPTION);
        assertFalse(debianControl.isValid());
    }

    @Test
    public void testIsValidMaintMissing() {
        DebianControl debianControl = new DebianControl();
        debianControl.setPackageName(FILE_PACKAGENAME);
        debianControl.setVersion(FILE_VERSION);
        debianControl.setArchitecture(FILE_ARCHITECTURE);
        debianControl.setDescriptionSynopsis(FILE_DESCR_SYNOPSIS);
        debianControl.setDescription(FILE_DESCRIPTION);
        assertFalse(debianControl.isValid());
    }

    @Test
    public void testIsValidDescSynMissing() {
        DebianControl debianControl = new DebianControl();
        debianControl.setPackageName(FILE_PACKAGENAME);
        debianControl.setVersion(FILE_VERSION);
        debianControl.setArchitecture(FILE_ARCHITECTURE);
        debianControl.setMaintainer(FILE_MAINTAINER);
        debianControl.setDescription(FILE_DESCRIPTION);
    }

    @Test
    public void testIsValidDescMissing() {
        DebianControl debianControl = new DebianControl();
        debianControl.setPackageName(FILE_PACKAGENAME);
        debianControl.setVersion(FILE_VERSION);
        debianControl.setArchitecture(FILE_ARCHITECTURE);
        debianControl.setMaintainer(FILE_MAINTAINER);
        debianControl.setDescriptionSynopsis(FILE_DESCR_SYNOPSIS);
    }

}
