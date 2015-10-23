package be.hobbiton.maven.lipamp.common;

import java.io.File;

import org.codehaus.plexus.util.StringUtils;

public class ArchiveEntry {
    public static final int INVALID_MODE = -1;
    private String name;
    private File file;
    private String userName;
    private String groupName;
    private int mode = INVALID_MODE;
    private ArchiveEntryType type;

    public ArchiveEntry(String name, File file, String userName, String groupName, int mode, ArchiveEntryType type) {
        super();
        this.name = name;
        this.file = file;
        this.userName = userName;
        this.groupName = groupName;
        setMode(mode);
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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

    public final void setMode(int mode) {
        this.mode = (mode > INVALID_MODE) ? mode : INVALID_MODE;
    }

    public static enum ArchiveEntryType {
        F, D, L, S
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.name);
    }

    @Override
    public String toString() {
        return String.format("%s %04o %8s/%-8s %s", getType(), getMode(), getUserName(), getGroupName(), getName());
    }
}
