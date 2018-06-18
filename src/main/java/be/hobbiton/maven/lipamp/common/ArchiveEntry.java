package be.hobbiton.maven.lipamp.common;

import org.codehaus.plexus.util.StringUtils;

import java.io.File;

public class ArchiveEntry {
    public static final String DOT = ".";
    public static final String SLASH = "/";
    public static final String DEFAULT_USERNAME = "root";
    public static final String DEFAULT_GROUPNAME = "root";
    public static final String DEFAULT_FILEMODE = "0644";
    public static final int DEFAULT_FILEMODE_VALUE = Integer.parseInt(DEFAULT_FILEMODE, 8);
    public static final String DEFAULT_DIRMODE = "0755";
    public static final int DEFAULT_DIRMODE_VALUE = Integer.parseInt(DEFAULT_DIRMODE, 8);
    public static final int INVALID_MODE = -1;
    private static final long INVALID_SIZE = -1L;
    private static final char[] OCHARS = {'r', 'x', 'w'};
    private String name;
    private String absoluteName;
    private File file;
    private String userName;
    private String groupName;
    private int mode = INVALID_MODE;
    private ArchiveEntryType type;
    private long size = INVALID_SIZE;

    public ArchiveEntry(String name, File file, String userName, String groupName, int mode, ArchiveEntryType type) {
        super();
        this.name = name;
        this.absoluteName = getAbsoluteName(name);
        this.file = file;
        this.userName = userName;
        this.groupName = groupName;
        setMode(mode);
        this.type = type;
    }

    static String getModeString(ArchiveEntryType type, int mode) {
        StringBuilder sb = new StringBuilder();
        sb.append(type.getRep());
        for (int i = 9; i > 0; i--) {
            char oChar = OCHARS[i % 3];
            int mask = 1 << (i - 1);
            sb.append((mode & mask) > 0 ? oChar : '-');
        }
        return sb.toString();
    }

    public static int fromModeString(String mode) {
        if (!StringUtils.isBlank(mode)) {
            try {
                return Integer.parseInt(mode, 8);
            } catch (NumberFormatException e) {
                return INVALID_MODE;
            }
        }
        return INVALID_MODE;
    }

    private String getAbsoluteName(String name) {
        if (StringUtils.isNotBlank(name) && (name.charAt(0) == '.')) {
            return name.substring(1);
        }
        return name;
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

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getMode() {
        return this.mode;
    }

    public final void setMode(int mode) {
        this.mode = (mode > INVALID_MODE) ? mode : INVALID_MODE;
    }

    public ArchiveEntryType getType() {
        return this.type;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.name);
    }

    public String getAbsoluteName() {
        return absoluteName;
    }

    @Override
    public String toString() {
        return String.format("%s %8s/%-8s %8d %s", getModeString(getType(), getMode()), getUserName(), getGroupName(), getSize(), getName());
    }

    public enum ArchiveEntryType {
        F('-'), D('d'), L('-'), S('l');
        private char rep;

        ArchiveEntryType(char rep) {
            this.rep = rep;
        }

        public char getRep() {
            return this.rep;
        }

    }
}
