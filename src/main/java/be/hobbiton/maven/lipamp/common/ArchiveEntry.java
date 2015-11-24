package be.hobbiton.maven.lipamp.common;

import java.io.File;

import org.codehaus.plexus.util.StringUtils;

public class ArchiveEntry {
    private static char[] OCHARS = { 'r', 'x', 'w' };
    public static final int INVALID_MODE = -1;
    public static final long INVALID_SIZE = -1L;
    private String name;
    private File file;
    private String userName;
    private String groupName;
    private int mode = INVALID_MODE;
    private ArchiveEntryType type;
    private long size = INVALID_SIZE;

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

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public static enum ArchiveEntryType {
        F('-'), D('d'), L('-'), S('l');
        private char rep;

        ArchiveEntryType(char rep) {
            this.rep = rep;
        }

        public char getRep() {
            return this.rep;
        }

    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.name);
    }

    protected static String getModeString(ArchiveEntryType type, int mode) {
        StringBuilder sb = new StringBuilder();
        sb.append(type.getRep());
        for (int i = 9; i > 0; i--) {
            char oChar = OCHARS[i % 3];
            int mask = 1 << (i - 1);
            sb.append((mode & mask) > 0 ? oChar : '-');
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("%s %8s/%-8s %8d %s", getModeString(getType(), getMode()), getUserName(), getGroupName(),
                getSize(), getName());
    }
}
