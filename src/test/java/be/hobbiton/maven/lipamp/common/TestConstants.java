package be.hobbiton.maven.lipamp.common;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.artifact.versioning.VersionRange;

import be.hobbiton.maven.lipamp.deb.DebInfo;
import be.hobbiton.maven.lipamp.plugin.DebianPackageMojo;

public final class TestConstants {
    public static final Path BASEPATH = Paths.get("src/test/data");
    public static final Path DEB_BASEPATH = BASEPATH.resolve(DebianPackageMojo.DEBIAN_RESOURCES_DIR_NAME);
    public static final Path CONF_BASEPATH = DEB_BASEPATH.resolve(DebianPackageMojo.DEBIAN_CONTROL_FILES_DIR_NAME);
    public static final Path CONTROL_PATH = CONF_BASEPATH.resolve(DebInfo.DebianInfoFile.CONTROL.getFilename());
    public static final File BASEDIR = BASEPATH.toFile();
    public static final File DEB_BASEDIR = DEB_BASEPATH.toFile();
    public static final File CONF_BASEDIR = CONF_BASEPATH.toFile();
    public static final File CONTROL_FILE = CONTROL_PATH.toFile();
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

    public static final String DEP_ARTIFACTID = "hiapp";
    public static final String DEP_GROUPID = "be.hobbiton.cloud";
    public static final String DEP_PACKAGING = "jar";
    public static final VersionRange DEP_VERSION = VersionRange.createFromVersion("1.0.0");
    public static final String DEP_DESTINATION_DIR = "/opt/hiapp/";
    public static final String ART_FILENAME = "hiapp-1.0.0.jar";
    public static final String ART_NOVERSION_FILENAME = "hiapp.jar";
    public static final String DEP_DESTINATION_FILE = DEP_DESTINATION_DIR.concat(ART_NOVERSION_FILENAME);
    public static final String DEP_SCOPE = "compile";

    private TestConstants() {
    }

}
