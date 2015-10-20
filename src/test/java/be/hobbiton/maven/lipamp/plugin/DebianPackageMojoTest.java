package be.hobbiton.maven.lipamp.plugin;

import static be.hobbiton.maven.lipamp.common.ArchiveEntryCollector.*;
import static be.hobbiton.maven.lipamp.common.TestConstants.*;
import static be.hobbiton.maven.lipamp.plugin.DebianPackageMojo.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Build;
import org.apache.maven.model.Developer;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.hobbiton.maven.lipamp.common.ArchiveEntry;
import be.hobbiton.maven.lipamp.common.Slf4jLogImpl;
import be.hobbiton.maven.lipamp.deb.DebInfo;
import be.hobbiton.maven.lipamp.deb.DebianPackage;

public class DebianPackageMojoTest {
    private static final String CONFIG_FOLDERNAME = "etc/hiapp";
    private static final String CONFIG_FOLDERPATH = DebianPackage.CURRENT_DIR + CONFIG_FOLDERNAME + "/";
    private static final String CONFIGMODE = "750";
    private static final int CONFIGMODE_VALUE = Integer.parseInt(CONFIGMODE, 8);
    private static final int DEFAULT_DIRMODE_VALUE = Integer.parseInt(DEFAULT_DIRMODE, 8);
    private static final String CONFIGGROUP = "higroup";
    private static final String CONFIGUSER = "hiuser";
    private static final Logger LOGGER = LoggerFactory.getLogger(DebianPackageMojoTest.class);
    private static final String EXPLICIT_MAINTAINER = "John Doe";
    private static final String MODEL_MAINTAINER_NAME = "Jane Doe";
    private static final String MODEL_MAINTAINER_EMAIL = "jane.doe@home.com";
    private static final String MODEL_MAINTAINER = MODEL_MAINTAINER_NAME + " <" + MODEL_MAINTAINER_EMAIL + ">";
    private static final String ARTIFACTID = "hiapp-pkg";
    private static final String GROUPID = "be.hobbiton.cloud";
    private static final String VERSION = "1.0.0";
    private static final String NAME = "Cloud hi server";
    private static final String DESCRIPTION = "Cloud hi server - Non Secure Implementation";
    private static final File PROJECT_FILE = new File("src/test/data/pom.xml");
    private static final File OUTPUT_DIR = new File("target/DebianPackageMojoTest");
    private static final File PACKAGE_FILE = new File(OUTPUT_DIR, ARTIFACTID + "-" + VERSION + ".deb");
    private static final String SYSTEM_USERNAME = "myusername";
    private static final List<Developer> DEVELOPERS = new ArrayList<Developer>();
    private static final FolderEntry[] FOLDERS = new FolderEntry[] {
            new FolderEntry("/var/log/hiapp", CONFIGUSER, CONFIGGROUP, CONFIGMODE) };
    private static final String DEP_FILEPATH = "./opt/hiapp/" + DEP_ARTIFACTID + ".jar";
    private static final String ART_GROUP = CONFIGGROUP;
    private static final String ART_USER = "bin";
    private static final ArtifactPackageEntry[] ARTIFACTS = new ArtifactPackageEntry[] { new ArtifactPackageEntry(
            DEP_ARTIFACTID, DEP_GROUPID, DEP_PACKAGING, DEP_DESTINATION, ART_USER, ART_GROUP, CONFIGMODE) };
    private static final DefaultArtifact DEP_ARTIFACT = new DefaultArtifact(DEP_GROUPID, DEP_ARTIFACTID, DEP_VERSION,
            "compile", DEP_PACKAGING, null, new DefaultArtifactHandler());
    private Model model;
    private Build build;
    private MavenProject project;
    private DebianPackageMojo mojo;

    static {
        Developer developer = new Developer();
        developer.setName(MODEL_MAINTAINER_NAME);
        developer.setEmail(MODEL_MAINTAINER_EMAIL);
        DEVELOPERS.add(developer);
        DEP_ARTIFACT.setFile(new File("src/test/data/hiapp-1.0.0.jar"));
    }

    @Before
    public void setUp() throws Exception {
        tearDown();
        this.model = new Model();
        this.model.setArtifactId(ARTIFACTID);
        this.model.setGroupId(GROUPID);
        this.model.setVersion(VERSION);
        this.build = new Build();
        this.build.setDirectory(OUTPUT_DIR.getAbsolutePath());
        this.project = new MavenProject(this.model);
        this.project.setBuild(this.build);
        this.mojo = new DebianPackageMojo();
        this.mojo.setLog(new Slf4jLogImpl());
        this.mojo.setProject(this.project);
        this.mojo.setDefaultUsername(DEFAULT_USERNAME);
        this.mojo.setDefaultGroupname(DEFAULT_GROUPNAME);
        this.mojo.setDefaultDirectoryMode(DEFAULT_DIRMODE);
        this.mojo.setDefaultFileMode(DEFAULT_FILEMODE);
        this.mojo.setDescriptionSynopsis(NAME);
        this.mojo.setDescription(DESCRIPTION);
        System.setProperty("user.name", SYSTEM_USERNAME);
    }

    @After
    public void tearDown() throws Exception {
        if (OUTPUT_DIR.isDirectory()) {
            FileUtils.cleanDirectory(OUTPUT_DIR);
            assertTrue(OUTPUT_DIR.delete());
        }
    }

    @Test
    public void testExecuteDefault() throws Exception {
        this.project.setFile(PROJECT_FILE);
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(PACKAGE_FILE);
        LOGGER.debug(debianInfo.toString());
        assertEquals(3, debianInfo.getControlFiles().size());
        assertEquals(6, debianInfo.getDataFiles().size());
        assertInPackage(debianInfo, CONFIG_FOLDERPATH, DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_DIRMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
    }

    @Test
    public void testExecuteWithAtts() throws Exception {
        AttributeSelector[] attributeSelectors = new AttributeSelector[] {
                new AttributeSelector("/**", ART_USER, ART_GROUP, CONFIGMODE) };
        this.project.setFile(PROJECT_FILE);
        this.mojo.setAttributes(attributeSelectors);
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(PACKAGE_FILE);
        LOGGER.debug(debianInfo.toString());
        assertEquals(3, debianInfo.getControlFiles().size());
        assertEquals(6, debianInfo.getDataFiles().size());
        assertInPackage(debianInfo, CONFIG_FOLDERPATH, ART_USER, ART_GROUP, CONFIGMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
    }

    @Test
    public void testExecuteWithFolders() throws Exception {
        this.project.setFile(PROJECT_FILE);
        this.mojo.setFolders(FOLDERS);
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(PACKAGE_FILE);
        LOGGER.debug(debianInfo.toString());
        assertEquals(3, debianInfo.getControlFiles().size());
        assertEquals(9, debianInfo.getDataFiles().size());
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
    }

    @Test
    public void testExecuteFull() throws Exception {
        this.model.setDevelopers(DEVELOPERS);
        File copiedSrc = new File(OUTPUT_DIR, "src");
        assertTrue(copiedSrc.mkdirs());
        FileUtils.copyDirectoryStructure(new File(PROJECT_FILE.getParentFile(), "src"), copiedSrc);
        assertTrue(new File(copiedSrc, "main/deb/DEBIAN/control").delete());
        this.project.setFile(copiedSrc);
        this.mojo.setFolders(FOLDERS);
        Set<Artifact> deps = new HashSet<Artifact>();
        deps.add(DEP_ARTIFACT);
        this.project.setDependencyArtifacts(deps);
        this.mojo.setArtifacts(ARTIFACTS);
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(PACKAGE_FILE);
        LOGGER.debug(debianInfo.toString());
        assertEquals(3, debianInfo.getControlFiles().size());
        assertEquals(12, debianInfo.getDataFiles().size());
        assertInPackage(debianInfo, DEP_FILEPATH, ART_USER, ART_GROUP, CONFIGMODE_VALUE);
        assertEquals(ARTIFACTID, debianInfo.getControl().getPackageName());
        assertEquals(VERSION, debianInfo.getControl().getVersion());
        assertEquals(DEFAULT_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(MODEL_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(NAME, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(DESCRIPTION, debianInfo.getControl().getDescription());
    }

    @Test
    public void testExecuteWithArtifact() throws Exception {
        this.project.setFile(PROJECT_FILE);
        Set<Artifact> deps = new HashSet<Artifact>();
        deps.add(DEP_ARTIFACT);
        this.project.setDependencyArtifacts(deps);
        this.mojo.setArtifacts(ARTIFACTS);
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(PACKAGE_FILE);
        LOGGER.debug(debianInfo.toString());
        assertEquals(3, debianInfo.getControlFiles().size());
        assertEquals(9, debianInfo.getDataFiles().size());
        assertInPackage(debianInfo, DEP_FILEPATH, ART_USER, ART_GROUP, CONFIGMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
    }

    @Test(expected = MojoFailureException.class)
    public void testExecuteWithArtifactDepNotFound() throws Exception {
        this.project.setFile(PROJECT_FILE);
        this.mojo.setArtifacts(ARTIFACTS);
        this.mojo.execute();
    }

    @Test(expected = MojoFailureException.class)
    public void testExecuteWithArtifactDepWrongType() throws Exception {
        this.project.setFile(PROJECT_FILE);
        Set<Artifact> deps = new HashSet<Artifact>();
        DefaultArtifact depArtifact = new DefaultArtifact(DEP_GROUPID, DEP_ARTIFACTID, DEP_VERSION, "compile", "war",
                null, new DefaultArtifactHandler());
        depArtifact.setFile(new File("src/test/data/hiapp-1.0.0.jar"));
        deps.add(depArtifact);
        this.project.setDependencyArtifacts(deps);
        this.mojo.setArtifacts(ARTIFACTS);
        this.mojo.execute();
    }

    @Test
    public void testExecuteChangeFolderAttributes() throws Exception {
        this.project.setFile(PROJECT_FILE);
        this.mojo.setFolders(new FolderEntry[] { new FolderEntry(CONFIG_FOLDERNAME, CONFIGUSER, CONFIGGROUP, null) });
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(PACKAGE_FILE);
        LOGGER.debug(debianInfo.toString());
        assertEquals(3, debianInfo.getControlFiles().size());
        assertEquals(6, debianInfo.getDataFiles().size());
        assertInPackage(debianInfo, CONFIG_FOLDERPATH, CONFIGUSER, CONFIGGROUP, DEFAULT_DIRMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
    }

    @Test
    public void testExecuteChangeFolderModeAttribute() throws Exception {
        this.project.setFile(PROJECT_FILE);
        this.mojo.setFolders(new FolderEntry[] { new FolderEntry(CONFIG_FOLDERNAME + "/", null, null, CONFIGMODE) });
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(PACKAGE_FILE);
        LOGGER.debug(debianInfo.toString());
        assertEquals(3, debianInfo.getControlFiles().size());
        assertEquals(6, debianInfo.getDataFiles().size());
        assertInPackage(debianInfo, CONFIG_FOLDERPATH, DEFAULT_USERNAME, DEFAULT_GROUPNAME, CONFIGMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
    }

    @Test
    public void testExecuteNoControl() throws Exception {
        File copiedSrc = new File(OUTPUT_DIR, "src");
        assertTrue(copiedSrc.mkdirs());
        FileUtils.copyDirectoryStructure(new File(PROJECT_FILE.getParentFile(), "src"), copiedSrc);
        assertTrue(new File(copiedSrc, "main/deb/DEBIAN/control").delete());
        this.project.setFile(copiedSrc);
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(PACKAGE_FILE);
        assertEquals(3, debianInfo.getControlFiles().size());
        assertEquals(6, debianInfo.getDataFiles().size());
        assertEquals(ARTIFACTID, debianInfo.getControl().getPackageName());
        assertEquals(VERSION, debianInfo.getControl().getVersion());
        assertEquals(DEFAULT_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(SYSTEM_USERNAME, debianInfo.getControl().getMaintainer());
        assertEquals(NAME, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(DESCRIPTION, debianInfo.getControl().getDescription());
    }

    @Test(expected = MojoFailureException.class)
    public void testNoPackageBasedir() throws Exception {
        this.project.setFile(new File("src/test/data/src/main/deb"));
        this.mojo.execute();
    }

    @Test
    public void testGetReleasedVersion() {
        assertEquals(VERSION, this.mojo.getVersion());
    }

    @Test
    public void testGetSnapshotVersion() {
        this.project.setVersion(VERSION + SNAPSHOT_SUFFIX);
        String pkgVersion = this.mojo.getVersion();
        Pattern versionPattern = Pattern.compile(VERSION + "-20[0-9]{12}");
        LOGGER.debug(pkgVersion);
        Matcher versionMatcher = versionPattern.matcher(pkgVersion);
        assertTrue("No match for " + pkgVersion, versionMatcher.matches());
    }

    @Test
    public void testGetPackageFile() throws Exception {
        assertEquals(PACKAGE_FILE.getAbsolutePath(), this.mojo.getPackageFile().getAbsolutePath());
    }

    @Test
    public void testGetSnapshotPackageFile() throws Exception {
        this.project.setVersion(VERSION + SNAPSHOT_SUFFIX);
        String pkgFilename = this.mojo.getPackageFile().getAbsolutePath();
        Pattern pkgFilenamePattern = Pattern.compile(".*/" + ARTIFACTID + "-" + VERSION + "-20[0-9]{12}\\.deb");
        LOGGER.debug(pkgFilename);
        Matcher pkgFilenameMatcher = pkgFilenamePattern.matcher(pkgFilename);
        assertTrue("No match for " + pkgFilename, pkgFilenameMatcher.matches());
    }

    @Test
    public void testGetMaintainerFromSystemProps() throws Exception {
        assertEquals(SYSTEM_USERNAME, this.mojo.getMaintainer());
    }

    @Test
    public void testGetMaintainerFromModel() throws Exception {
        this.model.setDevelopers(DEVELOPERS);
        assertEquals(MODEL_MAINTAINER, this.mojo.getMaintainer());
    }

    @Test
    public void testGetMaintainerExplicit() throws Exception {
        this.model.setDevelopers(DEVELOPERS);
        this.mojo.setMaintainer(EXPLICIT_MAINTAINER);
        assertEquals(EXPLICIT_MAINTAINER, this.mojo.getMaintainer());
    }

    private void assertInPackage(DebInfo debianInfo, String name, String username, String groupname, int mode) {
        boolean checked = false;
        for (ArchiveEntry entry : debianInfo.getDataFiles()) {
            if (name.equals(entry.getName())) {
                checked = true;
                assertEquals(username, entry.getUserName());
                assertEquals(groupname, entry.getGroupName());
                assertEquals(mode, entry.getMode());
            }
        }
        assertTrue(checked);
    }
}
