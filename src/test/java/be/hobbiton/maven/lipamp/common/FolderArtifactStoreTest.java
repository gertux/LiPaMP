package be.hobbiton.maven.lipamp.common;

import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static be.hobbiton.maven.lipamp.common.TestConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 */
public class FolderArtifactStoreTest {
    private static final Path OUTPUT_DIR = Paths.get("target/FolderArtifactStoreTest");
    private static final File OUTPUT_DIR_FILE = OUTPUT_DIR.toFile();
    private static final Path ARTIFACT_DEST = OUTPUT_DIR.resolve(DEP_FILENAME);
    private static final File ARTIFACT_DEST_FILE = ARTIFACT_DEST.toFile();
    private FolderArtifactStore store;

    @Before
    public void setUp() throws Exception {
        tearDown();
        assertTrue(OUTPUT_DIR_FILE.mkdirs());
        this.store = new FolderArtifactStore(OUTPUT_DIR, CUSTOM_USERNAME, CUSTOM_GROUPNAME, CUSTOM_FILE_MODE, CUSTOM_DIR_MODE);
    }

    @After
    public void tearDown() throws Exception {
        if (OUTPUT_DIR_FILE.isDirectory()) {
            FileUtils.cleanDirectory(OUTPUT_DIR_FILE);
            assertTrue(OUTPUT_DIR_FILE.delete());
        }
    }

    @Test
    public void toArchiveEntries() {
        List<ArchiveEntry> entries = this.store.toArchiveEntries(DEP_ARTIFACT).collect(Collectors.toList());
        assertEquals(3, entries.size());
        assertDirectoryInArchiveEntries(OUTPUT_DIR.getParent().toString(), entries, CUSTOM_USERNAME, CUSTOM_GROUPNAME, CUSTOM_DIR_MODE);
        assertDirectoryInArchiveEntries(OUTPUT_DIR.toString(), entries, CUSTOM_USERNAME, CUSTOM_GROUPNAME, CUSTOM_DIR_MODE);
        assertFileInArchiveEntries(ARTIFACT_DEST.toString(), DEP_FILESIZE, entries, CUSTOM_USERNAME, CUSTOM_GROUPNAME, CUSTOM_FILE_MODE);
    }

    @Test
    public void save() {
        assertFalse(ARTIFACT_DEST_FILE.exists());
        this.store.save(DEP_ARTIFACT);
        assertTrue(ARTIFACT_DEST_FILE.isFile());
    }
    @Test
    public void toPaths() {
        List<Path> entries = this.store.toPaths(DEP_ARTIFACT).collect(Collectors.toList());
        assertEquals(3, entries.size());
        assertEquals(OUTPUT_DIR.getParent(), entries.get(0));
        assertEquals(OUTPUT_DIR, entries.get(1));
        assertEquals(ARTIFACT_DEST, entries.get(2));
    }
}