package be.hobbiton.maven.lipamp.plugin;

import be.hobbiton.maven.lipamp.common.LinuxPackagingException;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        FolderEntry[] invalidEntries = {
        new FolderEntry(null, null, null, null),
        new FolderEntry(null, USERNAME, null, null),
        new FolderEntry(null, null, GROUPNAME, null),
        new FolderEntry(null, null, null, MODE),
        new FolderEntry(null, null, GROUPNAME, MODE),
        new FolderEntry(null, USERNAME, null, MODE),
        new FolderEntry(null, USERNAME, GROUPNAME, null),
        new FolderEntry(null, USERNAME, GROUPNAME, MODE)};
        for(FolderEntry folderEntry: invalidEntries) {
            try{
                folderEntry.isValid();
                fail("Expected failure for ".concat(folderEntry.toString()));
            } catch (LinuxPackagingException e) {
                // Expected
            }
        }
    }

    @Test(expected = LinuxPackagingException.class)
    public void testMiscInValid() {
        FolderEntry entry = new FolderEntry();
        entry.isValid();
    }

    @Test
    public void testMiscValid() {
        FolderEntry entry = new FolderEntry();
        entry.setPath(PATH);
        assertTrue(entry.isValid());
        assertTrue(entry.toString().contains(PATH));
    }
}
