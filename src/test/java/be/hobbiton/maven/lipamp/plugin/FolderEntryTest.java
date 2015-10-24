package be.hobbiton.maven.lipamp.plugin;

import static org.junit.Assert.*;

import org.junit.Test;

public class FolderEntryTest {
    private static final String USERNAME = "username";
    private static final String GROUPNAME = "groupname";
    private static final String MODE = "0755";
    private static final String PATH = "/etc/init";

    @Test
    public void testIsValid() {
        assertTrue(new FolderEntry(PATH, USERNAME, GROUPNAME, MODE).isValid());
        assertTrue(new FolderEntry(PATH, USERNAME, GROUPNAME, null).isValid());
        assertTrue(new FolderEntry(PATH, USERNAME, null, null).isValid());
        assertTrue(new FolderEntry(PATH, null, GROUPNAME, null).isValid());
        assertTrue(new FolderEntry(PATH, null, GROUPNAME, MODE).isValid());
        assertTrue(new FolderEntry(PATH, null, null, MODE).isValid());
        assertTrue(new FolderEntry(PATH, USERNAME, null, MODE).isValid());
        assertTrue(new FolderEntry(PATH, null, null, null).isValid());
    }

    @Test
    public void testIsInvalid() {
        assertFalse(new FolderEntry(null, null, null, null).isValid());
        assertFalse(new FolderEntry(null, USERNAME, null, null).isValid());
        assertFalse(new FolderEntry(null, null, GROUPNAME, null).isValid());
        assertFalse(new FolderEntry(null, null, null, MODE).isValid());
        assertFalse(new FolderEntry(null, null, GROUPNAME, MODE).isValid());
        assertFalse(new FolderEntry(null, USERNAME, null, MODE).isValid());
        assertFalse(new FolderEntry(null, USERNAME, GROUPNAME, null).isValid());
        assertFalse(new FolderEntry(null, USERNAME, GROUPNAME, MODE).isValid());
    }

    @Test
    public void testMiscValid() {
        FolderEntry entry = new FolderEntry();
        assertFalse(entry.isValid());
        entry.setPath(PATH);
        assertTrue(entry.isValid());
        assertTrue(entry.toString().contains(PATH));
    }
}
