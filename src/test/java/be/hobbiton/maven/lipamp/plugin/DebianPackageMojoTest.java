package be.hobbiton.maven.lipamp.plugin;

import be.hobbiton.maven.lipamp.common.ArchiveEntry;
import be.hobbiton.maven.lipamp.common.Slf4jLogImpl;
import be.hobbiton.maven.lipamp.deb.DebInfo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Developer;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static be.hobbiton.maven.lipamp.common.Constants.*;
import static be.hobbiton.maven.lipamp.common.TestConstants.*;
import static be.hobbiton.maven.lipamp.deb.DebInfo.DebianInfoFile.*;
import static be.hobbiton.maven.lipamp.plugin.DebianPackageMojo.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DebianPackageMojoTest {
    public static final String LINK = "sys";
    public static final String LINK_TARGET = "../var/usr";
    private static final String CONFFILES_PATH = "deb/DEBIAN/" + CONFFILES.getFilename();
    private static final String CONTROL_PATH = "deb/DEBIAN/" + CONTROL.getFilename();
    private static final String CONFIG_FOLDERNAME = "etc/hiapp";
    private static final String CONFIG_FOLDERPATH = CURRENT_DIR + CONFIG_FOLDERNAME + "/";
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
    private static final String PACKAGING = DEBIAN_PACKAGING_TYPE;
    private static final String NAME = "Cloud hi server";
    private static final String DESCRIPTION = "Cloud hi server - Non Secure Implementation";
    private static final String DEPENDS = "jdk, tomcat";
    private static final String URL = "http://hi.home.com";
    private static final String SECTION = "Java";
    private static final String PRIORITY = "optional";
    private static final File PROJECT_FILE = new File("src/test/data/pom.xml");
    private static final File OUTPUT_DIR = new File("target/DebianPackageMojoTest");
    private static final File RESOURCES_DIR = new File(OUTPUT_DIR, "classes");
    private static final File DESTINATION_DEB_RESOURCES_DIR = new File(RESOURCES_DIR, DEBIAN_RESOURCES_DIR_NAME);
    private static final String PACKAGE_FINALNAME = ARTIFACTID + "-" + VERSION;
    private static final String PACKAGE_FINAL_FILENAME = ARTIFACTID + "-" + VERSION + DEBIAN_FILE_EXTENSION;
    private static final String SYSTEM_USERNAME = "myusername";
    private static final List<Developer> DEVELOPERS = new ArrayList<>();
    private static final String FOLDER = "/var/log/hiapp";
    private static final String TOP_LINK = SLASH.concat(LINK);
    private static final String TOP_LINK_REL = DOT.concat(TOP_LINK);
    private static final String DEEP_LINK = "/usr/local/bin";
    private static final String DEEP_LINK_REL = DOT.concat(DEEP_LINK);
    private static final String DEEP_LINK_TARGET = "../../bin";
    private static final FolderEntry[] FOLDERS = new FolderEntry[]{new FolderEntry(FOLDER, CONFIGUSER, CONFIGGROUP, CONFIGMODE)};
    private static final LinkEntry[] LINKS = new LinkEntry[]{new LinkEntry(TOP_LINK, LINK_TARGET, null, null, null), new LinkEntry(DEEP_LINK,
            DEEP_LINK_TARGET, SYSTEM_USERNAME, CONFIGGROUP, CONFIGMODE)};
    private static final String DEP_FILEPATH = "./opt/hiapp/" + DEP_ARTIFACTID + ".jar";
    private static final String ART_GROUP = CONFIGGROUP;
    private static final String ART_USER = "bin";
    private static final ArtifactPackageEntry ARTIFACT = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, DEP_PACKAGING, null, DEP_DESTINATION_DIR);
    private static final ArtifactPackageEntry[] ARTIFACTS = new ArtifactPackageEntry[]{ARTIFACT};
    private static final Log PLUGIN_LOGGER = new Slf4jLogImpl();
    private static final File SOURCE_DEB_RESOURCES_DIR = new File(BASEDIR, DEBIAN_RESOURCES_DIR_NAME);
    private static final String CUSTOM_PACKAGENAME = "custPackageName";
    private static final String CUSTOM_VERSION = "3.3.2";
    private static final String CUSTOM_ARCHITECTURE = "sparc";
    private static final String CUSTOM_MAINTAINER = "JRR Tolkien";
    private static final String CUSTOM_DESCRIPTION_SYNOPSIS = "Custom app";
    private static final String CUSTOM_DESCRIPTION = "Custom app described";
    private static final String CUSTOM_HOMEPAGE = "http://www.middleearth.ac.uk";
    private static final String CUSTOM_SECTION = "Orcs";
    private static final String CUSTOM_PRIORITY = "extra";
    private static final String CUSTOM_DEPENDS = "evil-wizzard";
    private static final String DEPENDENCY_DESTINATION = "/lib";

    static {
        Developer developer = new Developer();
        developer.setName(MODEL_MAINTAINER_NAME);
        developer.setEmail(MODEL_MAINTAINER_EMAIL);
        DEVELOPERS.add(developer);
        ARTIFACT.setAttributes(ART_USER, ART_GROUP, CONFIGMODE);
    }

    private Model model;
    private MavenProject project;
    private DebianPackageMojo mojo;
    private Artifact artifact;

    @Before
    public void setUp() throws Exception {
        tearDown();
        assertTrue(RESOURCES_DIR.mkdirs());
        this.model = new Model();
        this.model.setArtifactId(ARTIFACTID);
        this.model.setGroupId(GROUPID);
        this.model.setVersion(VERSION);
        this.project = new MavenProject(this.model);
        this.project.setArtifacts(Arrays.stream(new Artifact[]{SUB_ARTIFACT, DEP_ARTIFACT}).collect(Collectors.toSet()));
        this.project.setDependencyArtifacts(Arrays.stream(new Artifact[]{DEP_ARTIFACT}).collect(Collectors.toSet()));
        this.artifact = new DefaultArtifact(GROUPID, ARTIFACTID, VersionRange.createFromVersion(VERSION), null, PACKAGING, null, new DefaultArtifactHandler()
                , false);
        this.project.setArtifact(this.artifact);
        this.mojo = new DebianPackageMojo();
        this.mojo.setLog(PLUGIN_LOGGER);
        this.mojo.setProject(this.project);
        this.mojo.setOutputDirectory(OUTPUT_DIR);
        this.mojo.setResourcesDirectory(RESOURCES_DIR);
        this.mojo.setFinalName(PACKAGE_FINALNAME);
        this.mojo.setPackageName(ARTIFACTID);
        this.mojo.setDefaultUsername(DEFAULT_USERNAME);
        this.mojo.setDefaultGroupname(DEFAULT_GROUPNAME);
        this.mojo.setDefaultDirectoryMode(DEFAULT_DIRMODE);
        this.mojo.setDefaultFileMode(DEFAULT_FILEMODE);
        this.mojo.setDescriptionSynopsis(NAME);
        this.mojo.setDescription(DESCRIPTION);
        this.mojo.setVersion(VERSION);
        this.mojo.setArchitecture(DEFAULT_ARCHITECTURE);
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
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();
        LOGGER.debug("PATH = {}", layout.pathOf(DEP_ARTIFACT));
        this.project.setFile(PROJECT_FILE);
        this.mojo.execute();
        File artifactFile = this.project.getArtifact().getFile();
        assertEquals(PACKAGE_FINAL_FILENAME, artifactFile.getName());
        DebInfo debianInfo = new DebInfo(artifactFile, PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(6, debianInfo.getDataFiles().size());
        assertInPackage(debianInfo, CONFIG_FOLDERPATH, DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_DIRMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(0, debianInfo.getControl().getInstalledSize());
    }

    @Test
    public void testExecuteWithFlatDeps() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();
        LOGGER.debug("PATH = {}", layout.pathOf(DEP_ARTIFACT));
        this.project.setFile(PROJECT_FILE);
        this.mojo.setDependencies(new Dependencies(DEPENDENCY_DESTINATION, "flat", null, null, null));
        this.mojo.execute();
        File artifactFile = this.project.getArtifact().getFile();
        assertEquals(PACKAGE_FINAL_FILENAME, artifactFile.getName());
        DebInfo debianInfo = new DebInfo(artifactFile, PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(9, debianInfo.getDataFiles().size());
        assertDirectoryInArchiveEntries(".".concat(DEPENDENCY_DESTINATION), debianInfo.getDataFiles(), DEFAULT_USERNAME, DEFAULT_GROUPNAME,
                DEFAULT_DIRMODE_VALUE);
        assertFileInArchiveEntries(".".concat(DEPENDENCY_DESTINATION).concat(File.separator).concat(SUB_FILENAME), SUB_FILESIZE, debianInfo.getDataFiles(),
                DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_FILEMODE_VALUE);
        assertFileInArchiveEntries(".".concat(DEPENDENCY_DESTINATION).concat(File.separator).concat(DEP_FILENAME), DEP_FILESIZE, debianInfo.getDataFiles(),
                DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_FILEMODE_VALUE);
        assertInPackage(debianInfo, CONFIG_FOLDERPATH, DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_DIRMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(0, debianInfo.getControl().getInstalledSize());
    }

    @Test
    public void testExecuteWithMavenDeps() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();
        LOGGER.debug("PATH = {}", layout.pathOf(DEP_ARTIFACT));
        this.project.setFile(PROJECT_FILE);
        Dependencies dependencies = new Dependencies();
        dependencies.setDestination(DEPENDENCY_DESTINATION);
        dependencies.setType("maven");
        dependencies.setUsername(CUSTOM_USERNAME);
        dependencies.setGroupname(CUSTOM_GROUPNAME);
        dependencies.setMode(ArchiveEntry.fromMode(CUSTOM_FILE_MODE));
        this.mojo.setDependencies(dependencies);
        this.mojo.execute();
        File artifactFile = this.project.getArtifact().getFile();
        assertEquals(PACKAGE_FINAL_FILENAME, artifactFile.getName());
        DebInfo debianInfo = new DebInfo(artifactFile, PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(17, debianInfo.getDataFiles().size());
        assertDirectoryInArchiveEntries(".".concat(DEPENDENCY_DESTINATION), debianInfo.getDataFiles(), CUSTOM_USERNAME, CUSTOM_GROUPNAME,
                DEFAULT_DIRMODE_VALUE);
        assertFileInArchiveEntries(".".concat(DEPENDENCY_DESTINATION).concat(File.separator).concat("hobbiton/hello/2.0.1/").concat(SUB_FILENAME),
                SUB_FILESIZE, debianInfo.getDataFiles(), CUSTOM_USERNAME, CUSTOM_GROUPNAME, CUSTOM_FILE_MODE);
        assertFileInArchiveEntries(".".concat(DEPENDENCY_DESTINATION).concat(File.separator).concat("be/hobbiton/cloud/hiapp/1.0.0/").concat(DEP_FILENAME),
                DEP_FILESIZE, debianInfo.getDataFiles(), CUSTOM_USERNAME, CUSTOM_GROUPNAME, CUSTOM_FILE_MODE);
        assertInPackage(debianInfo, CONFIG_FOLDERPATH, DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_DIRMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(0, debianInfo.getControl().getInstalledSize());
    }

    @Test
    public void testExecuteWithSymLinks() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();
        LOGGER.debug("PATH = {}", layout.pathOf(DEP_ARTIFACT));
        this.project.setFile(PROJECT_FILE);
        this.mojo.setLinks(LINKS);
        this.mojo.execute();
        File artifactFile = this.project.getArtifact().getFile();
        assertEquals(PACKAGE_FINAL_FILENAME, artifactFile.getName());
        DebInfo debianInfo = new DebInfo(artifactFile, PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(10, debianInfo.getDataFiles().size());
        assertLinkInArchiveEntries(TOP_LINK_REL, LINK_TARGET, debianInfo.getDataFiles(), DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_DIRMODE_VALUE);
        assertLinkInArchiveEntries(DEEP_LINK_REL, DEEP_LINK_TARGET, debianInfo.getDataFiles(), SYSTEM_USERNAME, CONFIGGROUP, CONFIGMODE_VALUE);
        assertInPackage(debianInfo, CONFIG_FOLDERPATH, DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_DIRMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(0, debianInfo.getControl().getInstalledSize());
    }

    @Test
    public void testExecuteWithBadSymLink() throws Exception {
        LinkEntry badLinkEntry = new LinkEntry();
        badLinkEntry.setPath(TOP_LINK);
        LOGGER.debug("Bad Link = {}", badLinkEntry);
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();
        LOGGER.debug("PATH = {}", layout.pathOf(DEP_ARTIFACT));
        this.project.setFile(PROJECT_FILE);
        this.mojo.setLinks(new LinkEntry[]{badLinkEntry});
        this.mojo.execute();
        File artifactFile = this.project.getArtifact().getFile();
        assertEquals(PACKAGE_FINAL_FILENAME, artifactFile.getName());
        DebInfo debianInfo = new DebInfo(artifactFile, PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(6, debianInfo.getDataFiles().size());
        assertInPackage(debianInfo, CONFIG_FOLDERPATH, DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_DIRMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(0, debianInfo.getControl().getInstalledSize());
    }

    @Test
    public void testExecuteWithSymLinkOnFilesystem() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        Files.createSymbolicLink(DESTINATION_DEB_RESOURCES_DIR.toPath().resolve(LINK), Paths.get(LINK_TARGET));
        ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();
        LOGGER.debug("PATH = {}", layout.pathOf(DEP_ARTIFACT));
        this.project.setFile(PROJECT_FILE);
        this.mojo.execute();
        File artifactFile = this.project.getArtifact().getFile();
        assertEquals(PACKAGE_FINAL_FILENAME, artifactFile.getName());
        DebInfo debianInfo = new DebInfo(artifactFile, PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(7, debianInfo.getDataFiles().size());
        assertLinkInArchiveEntries(TOP_LINK_REL, LINK_TARGET, debianInfo.getDataFiles(), DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_DIRMODE_VALUE);
        assertInPackage(debianInfo, CONFIG_FOLDERPATH, DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_DIRMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(0, debianInfo.getControl().getInstalledSize());
    }

    @Test
    public void testExecuteWithAtts() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        AttributeSelector[] attributeSelectors = new AttributeSelector[]{new AttributeSelector("/**", ART_USER, ART_GROUP, CONFIGMODE, false)};
        this.project.setFile(PROJECT_FILE);
        this.mojo.setAttributes(attributeSelectors);
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(this.project.getArtifact().getFile(), PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(6, debianInfo.getDataFiles().size());
        assertInPackage(debianInfo, CONFIG_FOLDERPATH, ART_USER, ART_GROUP, CONFIGMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(0, debianInfo.getControl().getInstalledSize());
    }

    @Test
    public void testExecuteWithFolders() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        this.project.setFile(PROJECT_FILE);
        this.mojo.setFolders(FOLDERS);
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(this.project.getArtifact().getFile(), PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(9, debianInfo.getDataFiles().size());
        File folder = new File("./".concat(FOLDER));
        assertDirectoryInArchiveEntries(String.valueOf(folder), debianInfo.getDataFiles(), CONFIGUSER, CONFIGGROUP, CONFIGMODE_VALUE);
        assertDirectoryInArchiveEntries(folder.getParent(), debianInfo.getDataFiles(), DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_DIRMODE_VALUE);
        assertDirectoryInArchiveEntries(folder.getParentFile().getParent(), debianInfo.getDataFiles(), DEFAULT_USERNAME, DEFAULT_GROUPNAME,
                DEFAULT_DIRMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(0, debianInfo.getControl().getInstalledSize());
    }

    @Test
    public void testExecuteFull() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        Files.createSymbolicLink(DESTINATION_DEB_RESOURCES_DIR.toPath().resolve(LINK), Paths.get(LINK_TARGET));
        this.model.setDevelopers(DEVELOPERS);
        assertTrue(new File(RESOURCES_DIR, CONTROL_PATH).delete());
        this.mojo.setDependencies(new Dependencies(DEPENDENCY_DESTINATION, "maven", CUSTOM_USERNAME, CUSTOM_GROUPNAME, null));
        this.mojo.setFolders(FOLDERS);
        this.mojo.setLinks(LINKS);
        this.mojo.setArtifacts(ARTIFACTS);
        this.mojo.setHomepage(URL);
        this.mojo.setSection(SECTION);
        this.mojo.setPriority(PRIORITY);
        this.mojo.setDepends(DEPENDS);
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(this.project.getArtifact().getFile(), PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(21, debianInfo.getDataFiles().size());
        assertDirectoryInArchiveEntries(".".concat(DEPENDENCY_DESTINATION), debianInfo.getDataFiles(), CUSTOM_USERNAME, CUSTOM_GROUPNAME,
                DEFAULT_DIRMODE_VALUE);
        assertFileInArchiveEntries(".".concat(DEPENDENCY_DESTINATION).concat(File.separator).concat("hobbiton/hello/2.0.1/").concat(SUB_FILENAME),
                SUB_FILESIZE, debianInfo.getDataFiles(), CUSTOM_USERNAME, CUSTOM_GROUPNAME, DEFAULT_FILEMODE_VALUE);
        assertLinkInArchiveEntries(TOP_LINK_REL, LINK_TARGET, debianInfo.getDataFiles(), DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_DIRMODE_VALUE);
        assertLinkInArchiveEntries(DEEP_LINK_REL, DEEP_LINK_TARGET, debianInfo.getDataFiles(), SYSTEM_USERNAME, CONFIGGROUP, CONFIGMODE_VALUE);
        File artifactFile = new File("./".concat(DEP_DESTINATION_FILE));
        assertFileInArchiveEntries(String.valueOf(artifactFile), DEP_FILESIZE, debianInfo.getDataFiles(), ART_USER, ART_GROUP, CONFIGMODE_VALUE);
        assertDirectoryInArchiveEntries(artifactFile.getParent(), debianInfo.getDataFiles(), DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_DIRMODE_VALUE);
        assertDirectoryInArchiveEntries(artifactFile.getParentFile().getParent(), debianInfo.getDataFiles(), DEFAULT_USERNAME, DEFAULT_GROUPNAME,
                DEFAULT_DIRMODE_VALUE);
        assertInPackage(debianInfo, DEP_FILEPATH, ART_USER, ART_GROUP, CONFIGMODE_VALUE);
        assertEquals(ARTIFACTID, debianInfo.getControl().getPackageName());
        assertEquals(VERSION, debianInfo.getControl().getVersion());
        assertEquals(DEFAULT_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(MODEL_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(NAME, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(5, debianInfo.getControl().getInstalledSize());
        assertEquals(URL, debianInfo.getControl().getHomepage());
        assertEquals(SECTION, debianInfo.getControl().getSection());
        assertEquals(PRIORITY, debianInfo.getControl().getPriority());
        assertEquals(DEPENDS, debianInfo.getControl().getDepends());
    }

    @Test
    public void testExecuteWithArtifactNoAtts() throws Exception {
        ArtifactPackageEntry[] artifacts = new ArtifactPackageEntry[]{new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, DEP_PACKAGING, null, DEP_DESTINATION_DIR)};
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        this.project.setFile(PROJECT_FILE);
        this.mojo.setArtifacts(artifacts);
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(this.project.getArtifact().getFile(), PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(9, debianInfo.getDataFiles().size());
        File artifactFile = new File("./".concat(DEP_DESTINATION_FILE));
        assertFileInArchiveEntries(String.valueOf(artifactFile), DEP_FILESIZE, debianInfo.getDataFiles(), DEFAULT_USERNAME, DEFAULT_GROUPNAME,
                DEFAULT_FILEMODE_VALUE);
        assertDirectoryInArchiveEntries(artifactFile.getParent(), debianInfo.getDataFiles(), DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_DIRMODE_VALUE);
        assertDirectoryInArchiveEntries(artifactFile.getParentFile().getParent(), debianInfo.getDataFiles(), DEFAULT_USERNAME, DEFAULT_GROUPNAME,
                DEFAULT_DIRMODE_VALUE);
        assertInPackage(debianInfo, DEP_FILEPATH, DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_FILEMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(0, debianInfo.getControl().getInstalledSize());
    }

    @Test
    public void testExecuteWithArtifact() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        this.project.setFile(PROJECT_FILE);
        this.mojo.setArtifacts(ARTIFACTS);
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(this.project.getArtifact().getFile(), PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(9, debianInfo.getDataFiles().size());
        File artifactFile = new File("./".concat(DEP_DESTINATION_FILE));
        assertFileInArchiveEntries(String.valueOf(artifactFile), DEP_FILESIZE, debianInfo.getDataFiles(), ART_USER, ART_GROUP, CONFIGMODE_VALUE);
        assertDirectoryInArchiveEntries(artifactFile.getParent(), debianInfo.getDataFiles(), DEFAULT_USERNAME, DEFAULT_GROUPNAME, DEFAULT_DIRMODE_VALUE);
        assertDirectoryInArchiveEntries(artifactFile.getParentFile().getParent(), debianInfo.getDataFiles(), DEFAULT_USERNAME, DEFAULT_GROUPNAME,
                DEFAULT_DIRMODE_VALUE);
        assertInPackage(debianInfo, DEP_FILEPATH, ART_USER, ART_GROUP, CONFIGMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(0, debianInfo.getControl().getInstalledSize());
    }

    @Test(expected = LinuxPackagingMojoException.class)
    public void testExecuteWithArtifactDepNotFound() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        this.project.setFile(PROJECT_FILE);
        this.project.setDependencyArtifacts(null);
        this.mojo.setArtifacts(ARTIFACTS);
        this.mojo.execute();
    }

    @Test(expected = LinuxPackagingMojoException.class)
    public void testExecuteWithArtifactDepWrongType() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        this.project.setFile(PROJECT_FILE);
        Set<Artifact> deps = new HashSet<Artifact>();
        DefaultArtifact depArtifact = new DefaultArtifact(DEP_GROUPID, DEP_ARTIFACTID, DEP_VERSION, "compile", "war", null, new DefaultArtifactHandler());
        depArtifact.setFile(new File("src/test/data/hiapp-1.0.0.jar"));
        deps.add(depArtifact);
        this.project.setDependencyArtifacts(deps);
        this.mojo.setArtifacts(ARTIFACTS);
        this.mojo.execute();
    }

    @Test
    public void testExecuteChangeFolderAttributes() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        this.project.setFile(PROJECT_FILE);
        this.mojo.setFolders(new FolderEntry[]{new FolderEntry(CONFIG_FOLDERNAME, CONFIGUSER, CONFIGGROUP, null)});
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(this.project.getArtifact().getFile(), PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(6, debianInfo.getDataFiles().size());
        assertInPackage(debianInfo, CONFIG_FOLDERPATH, CONFIGUSER, CONFIGGROUP, DEFAULT_DIRMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(0, debianInfo.getControl().getInstalledSize());
    }

    @Test
    public void testExecuteChangeFolderModeAttribute() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        this.project.setFile(PROJECT_FILE);
        this.mojo.setFolders(new FolderEntry[]{new FolderEntry(CONFIG_FOLDERNAME + "/", null, null, CONFIGMODE)});
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(this.project.getArtifact().getFile(), PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(6, debianInfo.getDataFiles().size());
        assertInPackage(debianInfo, CONFIG_FOLDERPATH, DEFAULT_USERNAME, DEFAULT_GROUPNAME, CONFIGMODE_VALUE);
        assertEquals(FILE_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(FILE_VERSION, debianInfo.getControl().getVersion());
        assertEquals(FILE_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(FILE_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(FILE_DESCR_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(FILE_DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(0, debianInfo.getControl().getInstalledSize());
    }

    @Test
    public void testExecuteNoControl() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        assertTrue(new File(RESOURCES_DIR, CONTROL_PATH).delete());
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(this.project.getArtifact().getFile(), PLUGIN_LOGGER);
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(6, debianInfo.getDataFiles().size());
        assertEquals(ARTIFACTID, debianInfo.getControl().getPackageName());
        assertEquals(VERSION, debianInfo.getControl().getVersion());
        assertEquals(DEFAULT_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(SYSTEM_USERNAME, debianInfo.getControl().getMaintainer());
        assertEquals(NAME, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(1, debianInfo.getControl().getInstalledSize());
    }

    @Test
    public void testExecuteNoControlAllParametersSet() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        assertTrue(new File(RESOURCES_DIR, CONTROL_PATH).delete());
        this.mojo.setPackageName(CUSTOM_PACKAGENAME);
        this.mojo.setVersion(CUSTOM_VERSION);
        this.mojo.setArchitecture(CUSTOM_ARCHITECTURE);
        this.mojo.setMaintainer(CUSTOM_MAINTAINER);
        this.mojo.setDescriptionSynopsis(CUSTOM_DESCRIPTION_SYNOPSIS);
        this.mojo.setDescription(CUSTOM_DESCRIPTION);
        this.mojo.setHomepage(CUSTOM_HOMEPAGE);
        this.mojo.setSection(CUSTOM_SECTION);
        this.mojo.setPriority(CUSTOM_PRIORITY);
        this.mojo.setDepends(CUSTOM_DEPENDS);
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(this.project.getArtifact().getFile(), PLUGIN_LOGGER);
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(6, debianInfo.getDataFiles().size());
        assertEquals(CUSTOM_PACKAGENAME, debianInfo.getControl().getPackageName());
        assertEquals(CUSTOM_VERSION, debianInfo.getControl().getVersion());
        assertEquals(CUSTOM_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(CUSTOM_MAINTAINER, debianInfo.getControl().getMaintainer());
        assertEquals(CUSTOM_DESCRIPTION_SYNOPSIS, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(CUSTOM_DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(1, debianInfo.getControl().getInstalledSize());
        assertEquals(CUSTOM_HOMEPAGE, debianInfo.getControl().getHomepage());
        assertEquals(CUSTOM_SECTION, debianInfo.getControl().getSection());
        assertEquals(CUSTOM_PRIORITY, debianInfo.getControl().getPriority());
        assertEquals(CUSTOM_DEPENDS, debianInfo.getControl().getDepends());
    }

    @Test
    public void testExecuteNoConfig() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        assertTrue(new File(RESOURCES_DIR, CONTROL_PATH).delete());
        assertTrue(new File(RESOURCES_DIR, CONFFILES_PATH).delete());
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(this.project.getArtifact().getFile(), PLUGIN_LOGGER);
        assertControl(debianInfo, new String[]{CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertEquals(6, debianInfo.getDataFiles().size());
        assertEquals(ARTIFACTID, debianInfo.getControl().getPackageName());
        assertEquals(VERSION, debianInfo.getControl().getVersion());
        assertEquals(DEFAULT_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(SYSTEM_USERNAME, debianInfo.getControl().getMaintainer());
        assertEquals(NAME, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(1, debianInfo.getControl().getInstalledSize());
    }

    @Test
    public void testExecuteNoConfigAtt() throws Exception {
        FileUtils.copyDirectoryStructure(SOURCE_DEB_RESOURCES_DIR, DESTINATION_DEB_RESOURCES_DIR);
        assertTrue(new File(RESOURCES_DIR, CONTROL_PATH).delete());
        assertTrue(new File(RESOURCES_DIR, CONFFILES_PATH).delete());
        AttributeSelector[] attributeSelectors = new AttributeSelector[]{new AttributeSelector("/**/*.conf", null, null, null, true)};
        this.mojo.setAttributes(attributeSelectors);
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(this.project.getArtifact().getFile(), PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertControl(debianInfo, new String[]{CONFFILES.getFilename(), CONTROL.getFilename(), POST_INSTALL.getFilename()});
        assertConffiles(debianInfo, new String[]{"/etc/hiapp/hiapp.conf", "/etc/init/hiapp.conf"});
        assertEquals(6, debianInfo.getDataFiles().size());
        assertEquals(ARTIFACTID, debianInfo.getControl().getPackageName());
        assertEquals(VERSION, debianInfo.getControl().getVersion());
        assertEquals(DEFAULT_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(SYSTEM_USERNAME, debianInfo.getControl().getMaintainer());
        assertEquals(NAME, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(1, debianInfo.getControl().getInstalledSize());
    }

    @Test(expected = MojoFailureException.class)
    public void testNothingToPackage() throws Exception {
        this.mojo.execute();
    }

    @Test
    public void testExecuteOnlyArtifact() throws Exception {
        this.project.setFile(PROJECT_FILE);
        Set<Artifact> deps = new HashSet<>();
        deps.add(DEP_ARTIFACT);
        this.project.setDependencyArtifacts(deps);
        this.mojo.setArtifacts(ARTIFACTS);
        this.mojo.execute();
        DebInfo debianInfo = new DebInfo(this.project.getArtifact().getFile(), PLUGIN_LOGGER);
        LOGGER.debug(debianInfo.toString());
        assertControl(debianInfo, new String[]{CONTROL.getFilename()});
        assertEquals(4, debianInfo.getDataFiles().size());
        assertInPackage(debianInfo, DEP_FILEPATH, ART_USER, ART_GROUP, CONFIGMODE_VALUE);
        assertEquals(ARTIFACTID, debianInfo.getControl().getPackageName());
        assertEquals(VERSION, debianInfo.getControl().getVersion());
        assertEquals(DEFAULT_ARCHITECTURE, debianInfo.getControl().getArchitecture());
        assertEquals(SYSTEM_USERNAME, debianInfo.getControl().getMaintainer());
        assertEquals(NAME, debianInfo.getControl().getDescriptionSynopsis());
        assertEquals(DESCRIPTION, debianInfo.getControl().getDescription());
        assertEquals(3, debianInfo.getControl().getInstalledSize());
    }


    @Test
    public void testGetReleasedVersion() {
        assertEquals(VERSION, this.mojo.getVersion());
    }

    @Test
    public void testGetSnapshotVersion() {
        this.mojo.setVersion(VERSION + SNAPSHOT_SUFFIX);
        String pkgVersion = this.mojo.getVersion();
        Pattern versionPattern = Pattern.compile(VERSION + "-20[0-9]{12}");
        LOGGER.debug(pkgVersion);
        Matcher versionMatcher = versionPattern.matcher(pkgVersion);
        assertTrue("No match for " + pkgVersion, versionMatcher.matches());
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

    @Test
    public void testGetSizeKB() {
        assertEquals(1, this.mojo.getSizeKB(1024));
        assertEquals(1, this.mojo.getSizeKB(102));
        assertEquals(2, this.mojo.getSizeKB(1025));
    }
}
