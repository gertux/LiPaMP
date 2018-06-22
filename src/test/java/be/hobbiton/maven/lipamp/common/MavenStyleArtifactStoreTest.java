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

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 */
public class MavenStyleArtifactStoreTest {
    private static final Path OUTPUT_DIR = Paths.get("target/MavenStyleArtifactStoreTest");
    private static final File OUTPUT_DIR_FILE = OUTPUT_DIR.toFile();
    private static final Path ARTIFACT_DEST = OUTPUT_DIR.resolve(Paths.get(DEP_GROUPID_PARTS[0], DEP_GROUPID_PARTS[1], DEP_GROUPID_PARTS[2], DEP_ARTIFACTID,DEP_VERSION_STR,DEP_FILENAME));
    private static final File ARTIFACT_DEST_FILE = ARTIFACT_DEST.toFile();
    private MavenStyleArtifactStore store;
    @Before
    public void setUp() throws Exception {
        tearDown();
        assertTrue(OUTPUT_DIR_FILE.mkdirs());
        this.store = new MavenStyleArtifactStore(OUTPUT_DIR, CUSTOM_USERNAME, CUSTOM_GROUPNAME, CUSTOM_FILE_MODE, CUSTOM_DIR_MODE);
    }

    @After
    public void tearDown() throws Exception {
        if (OUTPUT_DIR_FILE.isDirectory()) {
            FileUtils.cleanDirectory(OUTPUT_DIR_FILE);
            assertTrue(OUTPUT_DIR_FILE.delete());
        }
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
        assertEquals(8, entries.size());
        assertEquals(OUTPUT_DIR.getParent(), entries.get(0));
        assertEquals(OUTPUT_DIR, entries.get(1));
        assertEquals(ARTIFACT_DEST, entries.get(7));
    }
}