package be.hobbiton.maven.lipamp.common;

import static be.hobbiton.maven.lipamp.common.ArchiveEntryCollector.*;
import static org.junit.Assert.assertEquals;

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
    private static final File LEAF_FILE = new File("src/test/data/src/main/deb/etc/init/hiapp.conf");
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
        ArchiveEntry l0 = iterator.next();
        assertEquals("/", l0.getName());
        assertEquals(DEFAULT_USERNAME, l0.getUserName());
        assertEquals(DEFAULT_GROUPNAME, l0.getGroupName());
        assertEquals(DEFAULT_DIRMODE_VALUE, l0.getMode());
        assertEquals(ArchiveEntryType.D, l0.getType());
        ArchiveEntry l1 = iterator.next();
        assertEquals("/etc/", l1.getName());
        assertEquals(DEFAULT_USERNAME, l1.getUserName());
        assertEquals(DEFAULT_GROUPNAME, l1.getGroupName());
        assertEquals(DEFAULT_DIRMODE_VALUE, l1.getMode());
        assertEquals(ArchiveEntryType.D, l1.getType());
        ArchiveEntry l2 = iterator.next();
        assertEquals("/etc/init/", l2.getName());
        assertEquals(DEFAULT_USERNAME, l2.getUserName());
        assertEquals(DEFAULT_GROUPNAME, l2.getGroupName());
        assertEquals(DEFAULT_DIRMODE_VALUE, l2.getMode());
        assertEquals(ArchiveEntryType.D, l2.getType());
        ArchiveEntry l3 = iterator.next();
        assertEquals("/etc/init/app.conf", l3.getName());
        assertEquals(LEAF_USER, l3.getUserName());
        assertEquals(LEAF_GROUP, l3.getGroupName());
        assertEquals(LEAF_MODE, l3.getMode());
        assertEquals(ArchiveEntryType.F, l3.getType());
    }

    @Test
    public void testAddLeafFileNoSlash() {
        this.collector.add(new FileArchiveEntry("etc/init/app.conf", LEAF_FILE, LEAF_USER, LEAF_GROUP, LEAF_MODE));
        Collection<ArchiveEntry> entries = this.collector.getEntries();
        assertEquals(4, entries.size());
        Iterator<ArchiveEntry> iterator = entries.iterator();
        ArchiveEntry l0 = iterator.next();
        assertEquals("/", l0.getName());
        assertEquals(DEFAULT_USERNAME, l0.getUserName());
        assertEquals(DEFAULT_GROUPNAME, l0.getGroupName());
        assertEquals(DEFAULT_DIRMODE_VALUE, l0.getMode());
        assertEquals(ArchiveEntryType.D, l0.getType());
        ArchiveEntry l1 = iterator.next();
        assertEquals("/etc/", l1.getName());
        assertEquals(DEFAULT_USERNAME, l1.getUserName());
        assertEquals(DEFAULT_GROUPNAME, l1.getGroupName());
        assertEquals(DEFAULT_DIRMODE_VALUE, l1.getMode());
        assertEquals(ArchiveEntryType.D, l1.getType());
        ArchiveEntry l2 = iterator.next();
        assertEquals("/etc/init/", l2.getName());
        assertEquals(DEFAULT_USERNAME, l2.getUserName());
        assertEquals(DEFAULT_GROUPNAME, l2.getGroupName());
        assertEquals(DEFAULT_DIRMODE_VALUE, l2.getMode());
        assertEquals(ArchiveEntryType.D, l2.getType());
        ArchiveEntry l3 = iterator.next();
        assertEquals("/etc/init/app.conf", l3.getName());
        assertEquals(LEAF_USER, l3.getUserName());
        assertEquals(LEAF_GROUP, l3.getGroupName());
        assertEquals(LEAF_MODE, l3.getMode());
        assertEquals(ArchiveEntryType.F, l3.getType());
    }

    @Test
    public void testAddLeafDir() {
        this.collector.add(new DirectoryArchiveEntry("/etc/init", LEAF_USER, LEAF_GROUP, LEAF_MODE));
        Collection<ArchiveEntry> entries = this.collector.getEntries();
        assertEquals(3, entries.size());
        Iterator<ArchiveEntry> iterator = entries.iterator();
        ArchiveEntry l0 = iterator.next();
        assertEquals("/", l0.getName());
        assertEquals(DEFAULT_USERNAME, l0.getUserName());
        assertEquals(DEFAULT_GROUPNAME, l0.getGroupName());
        assertEquals(DEFAULT_DIRMODE_VALUE, l0.getMode());
        assertEquals(ArchiveEntryType.D, l0.getType());
        ArchiveEntry l1 = iterator.next();
        assertEquals("/etc/", l1.getName());
        assertEquals(DEFAULT_USERNAME, l1.getUserName());
        assertEquals(DEFAULT_GROUPNAME, l1.getGroupName());
        assertEquals(DEFAULT_DIRMODE_VALUE, l1.getMode());
        assertEquals(ArchiveEntryType.D, l1.getType());
        ArchiveEntry l2 = iterator.next();
        assertEquals("/etc/init/", l2.getName());
        assertEquals(LEAF_USER, l2.getUserName());
        assertEquals(LEAF_GROUP, l2.getGroupName());
        assertEquals(LEAF_MODE, l2.getMode());
        assertEquals(ArchiveEntryType.D, l2.getType());
    }

    @Test
    public void testAddLeafDirDot() {
        this.collector.add(new DirectoryArchiveEntry("./etc/init", LEAF_USER, LEAF_GROUP, LEAF_MODE));
        Collection<ArchiveEntry> entries = this.collector.getEntries();
        for (ArchiveEntry entry : entries) {
            LOGGER.debug(entry.toString());
        }
        assertEquals(3, entries.size());
        Iterator<ArchiveEntry> iterator = entries.iterator();
        ArchiveEntry l0 = iterator.next();
        assertEquals("/", l0.getName());
        assertEquals(DEFAULT_USERNAME, l0.getUserName());
        assertEquals(DEFAULT_GROUPNAME, l0.getGroupName());
        assertEquals(DEFAULT_DIRMODE_VALUE, l0.getMode());
        assertEquals(ArchiveEntryType.D, l0.getType());
        ArchiveEntry l1 = iterator.next();
        assertEquals("/etc/", l1.getName());
        assertEquals(DEFAULT_USERNAME, l1.getUserName());
        assertEquals(DEFAULT_GROUPNAME, l1.getGroupName());
        assertEquals(DEFAULT_DIRMODE_VALUE, l1.getMode());
        assertEquals(ArchiveEntryType.D, l1.getType());
        ArchiveEntry l2 = iterator.next();
        assertEquals("/etc/init/", l2.getName());
        assertEquals(LEAF_USER, l2.getUserName());
        assertEquals(LEAF_GROUP, l2.getGroupName());
        assertEquals(LEAF_MODE, l2.getMode());
        assertEquals(ArchiveEntryType.D, l2.getType());
    }

    @Test
    public void testMergeLeafFileLeafDir() {
        this.collector.add(new FileArchiveEntry("etc/init/app.conf", LEAF_FILE, LEAF_USER, LEAF_GROUP, LEAF_MODE));
        this.collector.add(new DirectoryArchiveEntry("/etc/init", LEAF_USER, LEAF_GROUP, LEAF_MODE));
        Collection<ArchiveEntry> entries = this.collector.getEntries();
        assertEquals(4, entries.size());
        Iterator<ArchiveEntry> iterator = entries.iterator();
        ArchiveEntry l0 = iterator.next();
        assertEquals("/", l0.getName());
        assertEquals(DEFAULT_USERNAME, l0.getUserName());
        assertEquals(DEFAULT_GROUPNAME, l0.getGroupName());
        assertEquals(DEFAULT_DIRMODE_VALUE, l0.getMode());
        assertEquals(ArchiveEntryType.D, l0.getType());
        ArchiveEntry l1 = iterator.next();
        assertEquals("/etc/", l1.getName());
        assertEquals(DEFAULT_USERNAME, l1.getUserName());
        assertEquals(DEFAULT_GROUPNAME, l1.getGroupName());
        assertEquals(DEFAULT_DIRMODE_VALUE, l1.getMode());
        assertEquals(ArchiveEntryType.D, l1.getType());
        ArchiveEntry l2 = iterator.next();
        assertEquals("/etc/init/", l2.getName());
        assertEquals(LEAF_USER, l2.getUserName());
        assertEquals(LEAF_GROUP, l2.getGroupName());
        assertEquals(LEAF_MODE, l2.getMode());
        assertEquals(ArchiveEntryType.D, l2.getType());
        ArchiveEntry l3 = iterator.next();
        assertEquals("/etc/init/app.conf", l3.getName());
        assertEquals(LEAF_USER, l3.getUserName());
        assertEquals(LEAF_GROUP, l3.getGroupName());
        assertEquals(LEAF_MODE, l3.getMode());
        assertEquals(ArchiveEntryType.F, l3.getType());
    }

    // @Test
    // public void testApplyAttributes() {
    // fail("Not yet implemented");
    // }

}
