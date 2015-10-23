package be.hobbiton.maven.lipamp.common;

import static be.hobbiton.maven.lipamp.common.ArchiveEntryCollector.*;
import static be.hobbiton.maven.lipamp.common.TestConstants.BASEDIR;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.hobbiton.maven.lipamp.common.ArchiveEntry.ArchiveEntryType;

public class ArchiveEntryCollectorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveEntryCollectorTest.class);
    private static final String LEAF_USER = "hiuser";
    private static final String LEAF_GROUP = "higroup";
    private static final int LEAF_MODE = Integer.parseInt("0400", 8);
    private static final File LEAF_FILE = new File(BASEDIR, "deb/etc/init/hiapp.conf");
    private static final File MISSING_FILE = new File("src/test/missing-file");
    private ArchiveEntryCollector collector;

    @Before
    public void setUp() throws Exception {
        this.collector = new ArchiveEntryCollector();
    }

    @Test
    public void testAddLeafFile() {
        this.collector.add(new FileArchiveEntry("/etc/init/app.conf", LEAF_FILE, LEAF_USER, LEAF_GROUP, LEAF_MODE));
        Collection<ArchiveEntry> entries = this.collector.getEntries();
        assertEquals(4, entries.size());
        Iterator<ArchiveEntry> iterator = entries.iterator();
        assertDefaultFolder(iterator.next(), "/");
        assertDefaultFolder(iterator.next(), "/etc/");
        assertDefaultFolder(iterator.next(), "/etc/init/");
        assertLeafFile(iterator.next(), "/etc/init/app.conf");
        assertEquals(353, this.collector.getInstalledSize());

        Collection<File> conffiles = this.collector.applyAttributes("/**", LEAF_USER, LEAF_GROUP, LEAF_MODE, false);
        assertTrue(conffiles.isEmpty());
        assertEquals(4, entries.size());
        Iterator<ArchiveEntry> afterAttsIterator = entries.iterator();
        assertLeafFolder(afterAttsIterator.next(), "/");
        assertLeafFolder(afterAttsIterator.next(), "/etc/");
        assertLeafFolder(afterAttsIterator.next(), "/etc/init/");
        assertLeafFile(afterAttsIterator.next(), "/etc/init/app.conf");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddLeafFileMissing() {
        assertFalse(MISSING_FILE.isFile());
        this.collector.add(new FileArchiveEntry("/etc/init/app.conf", MISSING_FILE, LEAF_USER, LEAF_GROUP, LEAF_MODE));
    }

    @Test
    public void testAddLeafFileNoAtts() {
        this.collector
        .add(new FileArchiveEntry("/etc/init/app.conf", LEAF_FILE, null, null, ArchiveEntry.INVALID_MODE));
        Collection<ArchiveEntry> entries = this.collector.getEntries();
        assertEquals(4, entries.size());
        Iterator<ArchiveEntry> iterator = entries.iterator();
        assertDefaultFolder(iterator.next(), "/");
        assertDefaultFolder(iterator.next(), "/etc/");
        assertDefaultFolder(iterator.next(), "/etc/init/");
        assertDefaultFile(iterator.next(), "/etc/init/app.conf");
        assertEquals(353, this.collector.getInstalledSize());

        Collection<File> conffiles = this.collector.applyAttributes("%regex[.+[a-c]*/$]", LEAF_USER, LEAF_GROUP,
                LEAF_MODE, false);
        assertTrue(conffiles.isEmpty());
        assertEquals(4, entries.size());
        Iterator<ArchiveEntry> afterREAttsIterator = entries.iterator();
        assertDefaultFolder(afterREAttsIterator.next(), "/");
        assertLeafFolder(afterREAttsIterator.next(), "/etc/");
        assertLeafFolder(afterREAttsIterator.next(), "/etc/init/");
        assertDefaultFile(afterREAttsIterator.next(), "/etc/init/app.conf");

        Collection<File> conffiles2 = this.collector.applyAttributes("/**/app.conf", LEAF_USER, LEAF_GROUP, LEAF_MODE,
                true);
        assertTrue(conffiles.isEmpty());
        assertEquals(1, conffiles2.size());
        assertEquals(4, entries.size());
        Iterator<ArchiveEntry> afterAttsIterator = entries.iterator();
        assertDefaultFolder(afterAttsIterator.next(), "/");
        assertLeafFolder(afterAttsIterator.next(), "/etc/");
        assertLeafFolder(afterAttsIterator.next(), "/etc/init/");
        assertLeafFile(afterAttsIterator.next(), "/etc/init/app.conf");
    }

    @Test
    public void testAddLeafFileNoSlash() {
        this.collector.add(new FileArchiveEntry("etc/init/app.conf", LEAF_FILE, LEAF_USER, LEAF_GROUP, LEAF_MODE));
        Collection<ArchiveEntry> entries = this.collector.getEntries();
        assertEquals(4, entries.size());
        Iterator<ArchiveEntry> iterator = entries.iterator();
        assertDefaultFolder(iterator.next(), "/");
        assertDefaultFolder(iterator.next(), "/etc/");
        assertDefaultFolder(iterator.next(), "/etc/init/");
        assertLeafFile(iterator.next(), "/etc/init/app.conf");
        assertEquals(353, this.collector.getInstalledSize());
        Collection<File> conffiles = this.collector.applyAttributes("%regex[^.*$]", null, null,
                ArchiveEntry.INVALID_MODE, true);
        assertFalse(conffiles.isEmpty());
        assertEquals(1, conffiles.size());
    }

    @Test
    public void testAddLeafDir() {
        this.collector.add(new DirectoryArchiveEntry("/etc/init", LEAF_USER, LEAF_GROUP, LEAF_MODE));
        Collection<ArchiveEntry> entries = this.collector.getEntries();
        assertEquals(3, entries.size());
        Iterator<ArchiveEntry> iterator = entries.iterator();
        assertDefaultFolder(iterator.next(), "/");
        assertDefaultFolder(iterator.next(), "/etc/");
        assertLeafFolder(iterator.next(), "/etc/init/");
        assertEquals(0, this.collector.getInstalledSize());
    }

    @Test
    public void testAddLeafDirNoAtts() {
        this.collector.add(new DirectoryArchiveEntry("/etc/init", null, null, -2));
        Collection<ArchiveEntry> entries = this.collector.getEntries();
        assertEquals(3, entries.size());
        Iterator<ArchiveEntry> iterator = entries.iterator();
        assertDefaultFolder(iterator.next(), "/");
        assertDefaultFolder(iterator.next(), "/etc/");
        assertDefaultFolder(iterator.next(), "/etc/init/");
        assertEquals(0, this.collector.getInstalledSize());
    }

    @Test
    public void testAddLeafDirDot() {
        this.collector.add(new DirectoryArchiveEntry("./etc/init", LEAF_USER, LEAF_GROUP, LEAF_MODE));
        Collection<ArchiveEntry> entries = this.collector.getEntries();
        assertEquals(3, entries.size());
        Iterator<ArchiveEntry> iterator = entries.iterator();
        assertDefaultFolder(iterator.next(), "/");
        assertDefaultFolder(iterator.next(), "/etc/");
        assertLeafFolder(iterator.next(), "/etc/init/");
        assertEquals(0, this.collector.getInstalledSize());
    }

    @Test
    public void testMergeLeafFileLeafDir() {
        this.collector.add(new FileArchiveEntry("etc/init/app.conf", LEAF_FILE, LEAF_USER, LEAF_GROUP, LEAF_MODE));
        this.collector.add(new DirectoryArchiveEntry("/etc/init", LEAF_USER, LEAF_GROUP, LEAF_MODE));
        Collection<ArchiveEntry> entries = this.collector.getEntries();
        assertEquals(4, entries.size());
        Iterator<ArchiveEntry> iterator = entries.iterator();
        assertDefaultFolder(iterator.next(), "/");
        assertDefaultFolder(iterator.next(), "/etc/");
        assertLeafFolder(iterator.next(), "/etc/init/");
        assertLeafFile(iterator.next(), "/etc/init/app.conf");
        assertEquals(353, this.collector.getInstalledSize());
    }

    private void assertArchiveEntry(ArchiveEntry entry, String path, String username, String groupname, int mode,
            ArchiveEntryType type) {
        LOGGER.debug(entry.toString());
        assertEquals(path, entry.getName());
        assertEquals(username, entry.getUserName());
        assertEquals(groupname, entry.getGroupName());
        assertEquals(mode, entry.getMode());
        assertEquals(type, entry.getType());
    }

    private void assertDefaultFolder(ArchiveEntry entry, String path) {
        assertArchiveEntry(entry, path, DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_DIRMODE_VALUE, ArchiveEntryType.D);
    }

    private void assertDefaultFile(ArchiveEntry entry, String path) {
        assertArchiveEntry(entry, path, DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_FILEMODE_VALUE,
                ArchiveEntryType.F);
    }

    private void assertLeafFolder(ArchiveEntry entry, String path) {
        assertArchiveEntry(entry, path, LEAF_USER, LEAF_GROUP, LEAF_MODE, ArchiveEntryType.D);
    }

    private void assertLeafFile(ArchiveEntry entry, String path) {
        assertArchiveEntry(entry, path, LEAF_USER, LEAF_GROUP, LEAF_MODE, ArchiveEntryType.F);
    }
}
