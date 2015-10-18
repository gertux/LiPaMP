package be.hobbiton.maven.lipamp.common;

import java.io.File;

public final class TestConstants {
    public static final File BASEDIR = new File("src/test/data/src/main/deb");
    public static final File CONF_BASEDIR = new File(BASEDIR, "DEBIAN");
    public static final File CONTROL_FILE = new File(CONF_BASEDIR, "control");
    public static final String FILE_DEPENDS = "oracle-java8";
    public static final String FILE_DESCRIPTION = "Cloud hello server - Non Secure Implementation";
    public static final String FILE_DESCR_SYNOPSIS = "Cloud hello server";
    public static final String FILE_VERSION = "1.0.1";
    public static final String FILE_INSTALLED_SIZE = "0";
    public static final String FILE_MAINTAINER = "Gert Dewit";
    public static final String FILE_PRIORITY = "optional";
    public static final String FILE_SECTION = "devel";
    public static final String FILE_PACKAGENAME = "hiapp-deb-pkg";
    public static final String FILE_ARCHITECTURE = "all";

    private TestConstants() {
    }

}
