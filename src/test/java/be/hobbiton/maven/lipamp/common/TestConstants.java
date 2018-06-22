package be.hobbiton.maven.lipamp.common;

import be.hobbiton.maven.lipamp.deb.DebInfo;
import be.hobbiton.maven.lipamp.plugin.DebianPackageMojo;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static be.hobbiton.maven.lipamp.common.Constants.INVALID_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class TestConstants {
    public static final Path BASEPATH = Paths.get("src/test/data");
    public static final Path DEB_BASEPATH = BASEPATH.resolve(DebianPackageMojo.DEBIAN_RESOURCES_DIR_NAME);
    public static final Path CONF_BASEPATH = DEB_BASEPATH.resolve(DebianPackageMojo.DEBIAN_CONTROL_FILES_DIR_NAME);
    public static final Path CONTROL_PATH = CONF_BASEPATH.resolve(DebInfo.DebianInfoFile.CONTROL.getFilename());
    public static final String DEP_ARTIFACT_FILENAME = "hiapp-1.0.0.jar";
    public static final Path DEP_ARTIFACT_PATH = BASEPATH.resolve(DEP_ARTIFACT_FILENAME);
    public static final String SUB_ARTIFACT_FILENAME = "hello-2.0.1.jar";
    public static final Path SUB_ARTIFACT_PATH = BASEPATH.resolve(SUB_ARTIFACT_FILENAME);
    public static final File BASEDIR = BASEPATH.toFile();
    public static final File DEB_BASEDIR = DEB_BASEPATH.toFile();
    public static final File CONF_BASEDIR = CONF_BASEPATH.toFile();
    public static final File CONTROL_FILE = CONTROL_PATH.toFile();
    public static final File DEP_ARTIFACT_FILE = DEP_ARTIFACT_PATH.toFile();
    public static final File SUB_ARTIFACT_FILE = SUB_ARTIFACT_PATH.toFile();
    public static final String FILE_DEPENDS = "oracle-java8";
    public static final String FILE_DESCRIPTION = "Cloud hello server - Non Secure Implementation";
    public static final String FILE_DESCR_SYNOPSIS = "Cloud hello server";
    public static final String FILE_VERSION = "1.0.1";
    public static final long FILE_INSTALLED_SIZE = 0L;
    public static final String FILE_MAINTAINER = "Gert Dewit";
    public static final String FILE_PRIORITY = "optional";
    public static final String FILE_SECTION = "devel";
    public static final String FILE_PACKAGENAME = "hiapp-deb-pkg";
    public static final String FILE_ARCHITECTURE = "all";

    public static final String SUB_VERSION_STR = "2.0.1";
    public static final VersionRange SUB_VERSION = VersionRange.createFromVersion(SUB_VERSION_STR);
    public static final String SUB_GROUPID = "hobbiton";
    public static final String SUB_ARTIFACTID = "hello";
    public static final String SUB_PACKAGING = "jar";
    public static final String SUB_CLASSIFIER = "jdk8";
    public static final DefaultArtifact SUB_ARTIFACT = new DefaultArtifact(SUB_GROUPID, SUB_ARTIFACTID, SUB_VERSION, "compile", SUB_PACKAGING,
            SUB_CLASSIFIER, new DefaultArtifactHandler(SUB_PACKAGING));
    public static final String SUB_FILENAME = SUB_ARTIFACTID.concat("-").concat(SUB_VERSION_STR).concat("-").concat(SUB_CLASSIFIER).concat(".").concat
            (SUB_PACKAGING);
    public static final int SUB_FILESIZE = 1279;


    public static final String DEP_ARTIFACTID = "hiapp";
    public static final String[] DEP_GROUPID_PARTS = {"be", "hobbiton", "cloud"};
    public static final String DEP_GROUPID = String.join(".", DEP_GROUPID_PARTS);
    public static final String DEP_PACKAGING = "jar";
    public static final String DEP_VERSION_STR = "1.0.0";
    public static final String DEP_FILENAME = DEP_ARTIFACTID.concat("-").concat(DEP_VERSION_STR).concat(".").concat(DEP_PACKAGING);
    public static final VersionRange DEP_VERSION = VersionRange.createFromVersion(DEP_VERSION_STR);
    public static final String DEP_DESTINATION_DIR = "/opt/hiapp/";
    public static final String ARTIFACT_NOVERSION_FILENAME = "hiapp.jar";
    public static final String DEP_DESTINATION_FILE = DEP_DESTINATION_DIR.concat(ARTIFACT_NOVERSION_FILENAME);
    public static final String DEP_SCOPE = "compile";
    public static final DefaultArtifact DEP_ARTIFACT = new DefaultArtifact(DEP_GROUPID, DEP_ARTIFACTID, DEP_VERSION, "compile", DEP_PACKAGING, null, new
            DefaultArtifactHandler(DEP_PACKAGING));
    public static final int DEP_FILESIZE = 2626;
    public static final String CUSTOM_USERNAME = "custom";
    public static final String CUSTOM_GROUPNAME = "customs";
    public static final int CUSTOM_FILE_MODE = 0222;
    public static final int CUSTOM_DIR_MODE = 0333;

    static {
        DEP_ARTIFACT.setFile(DEP_ARTIFACT_FILE);
        SUB_ARTIFACT.setFile(SUB_ARTIFACT_FILE);
    }

    private TestConstants() {
    }

    public static void assertInPackage(DebInfo debianInfo, String name, String username, String groupname, int mode) {
        boolean found = false;
        for (ArchiveEntry entry : debianInfo.getDataFiles()) {
            if (name.equals(entry.getName())) {
                assertAttributes(username, groupname, mode, entry);
                found = true;
            }
        }
        assertTrue(found);
    }

    public static void assertLinkInArchiveEntries(String linkName, String linkTarget, Collection<ArchiveEntry> dataFiles, String username, String groupname,
                                                  int mode) {
        assertInArchiveEntries(linkName, linkTarget, INVALID_SIZE, dataFiles, ArchiveEntry.ArchiveEntryType.S, username, groupname, mode);
    }

    public static void assertFileInArchiveEntries(String name, long size, Collection<ArchiveEntry> dataFiles, String username, String groupname, int mode) {
        assertInArchiveEntries(name, null, size, dataFiles, ArchiveEntry.ArchiveEntryType.F, username, groupname, mode);
    }

    public static void assertDirectoryInArchiveEntries(String name, Collection<ArchiveEntry> dataFiles, String username, String groupname, int mode) {
        assertInArchiveEntries(name.concat("/"), null, INVALID_SIZE, dataFiles, ArchiveEntry.ArchiveEntryType.D, username, groupname, mode);
    }

    public static void assertInArchiveEntries(String name, String link, long size, Collection<ArchiveEntry> entries, ArchiveEntry.ArchiveEntryType type,
                                              String username, String groupname, int mode) {
        boolean found = false;
        for (ArchiveEntry entry : entries) {
            if (name.equals(entry.getName())) {
                found = assertArchiveEntryEquals(entry, link, size, type, username, groupname, mode);
            }
        }
        assertTrue(found);
    }


    public static void assertAttributes(String username, String groupname, int mode, ArchiveEntry entry) {
        assertEquals(username, entry.getUserName());
        assertEquals(groupname, entry.getGroupName());
        assertEquals(mode, entry.getMode());
    }

    public static boolean assertArchiveEntryEquals(ArchiveEntry entry, String link, long size, ArchiveEntry.ArchiveEntryType type, String username,
                                                   String groupname, int mode) {
        switch (type) {
            case S:
                if (ArchiveEntry.ArchiveEntryType.S.equals(entry.getType())) {
                    SymbolicLinkArchiveEntry symlink = (SymbolicLinkArchiveEntry) entry;
                    assertEquals(link, symlink.getTarget());
                    assertAttributes(username, groupname, mode, symlink);
                    return true;
                }
                break;
            case D:
                if (ArchiveEntry.ArchiveEntryType.D.equals(entry.getType())) {
                    DirectoryArchiveEntry dir = (DirectoryArchiveEntry) entry;
                    assertAttributes(username, groupname, mode, dir);
                    return true;
                }
                break;
            case F:
                if (ArchiveEntry.ArchiveEntryType.F.equals(entry.getType())) {
                    FileArchiveEntry file = (FileArchiveEntry) entry;
                    assertEquals(size, file.getSize());
                    assertAttributes(username, groupname, mode, file);
                    return true;
                }
                break;
            default:
                return false;
        }
        return false;
    }


}
