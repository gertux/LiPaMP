package be.hobbiton.maven.lipamp.common;

public class DirectoryArchiveEntry extends ArchiveEntry {
    private static final String DIR_SUFFIX = "/";

    public DirectoryArchiveEntry(String name, String userName, String groupName, int mode) {
        super(name.endsWith(DIR_SUFFIX) ? name : name + DIR_SUFFIX, null, userName, groupName, mode, ArchiveEntryType.D);
        setSize(0);
    }
}
