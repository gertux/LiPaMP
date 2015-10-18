package be.hobbiton.maven.lipamp.common;

import java.io.File;

public class ArchiveEntry {
    private String name;
    private File file;
    private String userName;
    private String groupName;
    private int mode;
    private ArchiveEntryType type;

    public ArchiveEntry(String name, File file, String userName, String groupName, int mode, ArchiveEntryType type) {
        super();
        this.name = name;
        this.file = file;
        this.userName = userName;
        this.groupName = groupName;
        this.mode = mode;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public File getFile() {
        return this.file;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public int getMode() {
        return this.mode;
    }

    public ArchiveEntryType getType() {
        return this.type;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public static enum ArchiveEntryType {
        F, D, L, S
    }
}
