package be.hobbiton.maven.lipamp.common;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.plexus.util.SelectorUtils;
import org.codehaus.plexus.util.StringUtils;

import be.hobbiton.maven.lipamp.common.ArchiveEntry.ArchiveEntryType;

public class ArchiveEntryCollector {
    public static final String DEFAULT_USERNAME = "root";
    public static final String DEFAULT_GROUPNAME = "root";
    public static final String DEFAULT_FILEMODE = "0644";
    public static final int DEFAULT_FILEMODE_VALUE = Integer.parseInt(DEFAULT_FILEMODE, 8);
    public static final String DEFAULT_DIRMODE = "0755";
    public static final int DEFAULT_DIRMODE_VALUE = Integer.parseInt(DEFAULT_DIRMODE, 8);
    private String defaultUsername = DEFAULT_USERNAME;
    private String defaultGroupname = DEFAULT_GROUPNAME;
    private int defaultFilemode = DEFAULT_FILEMODE_VALUE;
    private int defaultDirmode = DEFAULT_DIRMODE_VALUE;
    private Map<String, ArchiveEntry> entries = new TreeMap<String, ArchiveEntry>();

    public void add(ArchiveEntry entry) {
        if (!entry.isValid()) {
            throw new IllegalArgumentException("Invalid entry: " + entry);
        }
        entry.setName(cleanPath(entry.getName(), entry.getType()));
        mergeEntries(this.entries.get(entry.getName()), entry);
        addParent(new File(entry.getName()));
    }

    private void addParent(File entryFile) {
        if (entryFile.getParentFile() != null) {
            DirectoryArchiveEntry parentEntry = new DirectoryArchiveEntry(entryFile.getParentFile().getAbsolutePath(),
                    this.defaultUsername, this.defaultGroupname, this.defaultDirmode);
            this.entries.put(parentEntry.getName(), parentEntry);
            addParent(entryFile.getParentFile());
        }
    }

    private void mergeEntries(ArchiveEntry existingEntry, ArchiveEntry newEntry) {
        if (existingEntry != null) {
            if (StringUtils.isNotBlank(newEntry.getUserName())) {
                existingEntry.setUserName(newEntry.getUserName());
            }
            if (StringUtils.isNotBlank(newEntry.getGroupName())) {
                existingEntry.setGroupName(newEntry.getGroupName());
            }
            if (newEntry.getMode() > ArchiveEntry.INVALID_MODE) {
                existingEntry.setMode(newEntry.getMode());
            }
        } else {
            if (StringUtils.isBlank(newEntry.getUserName())) {
                newEntry.setUserName(this.defaultUsername);
            }
            if (StringUtils.isBlank(newEntry.getGroupName())) {
                newEntry.setGroupName(this.defaultGroupname);
            }
            if (newEntry.getMode() <= ArchiveEntry.INVALID_MODE) {
                newEntry.setMode(
                        (newEntry.getType().equals(ArchiveEntryType.D)) ? this.defaultDirmode : this.defaultFilemode);
            }
            this.entries.put(newEntry.getName(), newEntry);
        }
    }

    private String cleanPath(String path, ArchiveEntryType type) {
        String newPath = path.trim();
        if (type.equals(ArchiveEntryType.D) && !newPath.endsWith("/")) {
            newPath = newPath + "/";
        }
        if (newPath.startsWith(".")) {
            newPath = newPath.substring(1);
        }
        if (!newPath.startsWith("/")) {
            newPath = "/" + newPath;
        }
        return newPath;
    }

    public void applyAttributes(String pattern, String username, String groupname, int mode) {
        for (ArchiveEntry entry : this.entries.values()) {
            if (SelectorUtils.matchPath(pattern, entry.getName())) {
                if (StringUtils.isNotBlank(username)) {
                    entry.setUserName(username);
                }
                if (StringUtils.isNotBlank(groupname)) {
                    entry.setGroupName(groupname);
                }
                if (mode > ArchiveEntry.INVALID_MODE) {
                    entry.setMode(mode);
                }
            }
        }
    }

    public Collection<ArchiveEntry> getEntries() {
        return this.entries.values();
    }

    public void setDefaultUsername(String defaultUsername) {
        this.defaultUsername = StringUtils.isNotBlank(defaultUsername) ? defaultUsername : DEFAULT_USERNAME;
    }

    public void setDefaultGroupname(String defaultGroupname) {
        this.defaultGroupname = StringUtils.isNotBlank(defaultGroupname) ? defaultGroupname : DEFAULT_GROUPNAME;
    }

    public void setDefaultFilemode(int defaultFilemode) {
        this.defaultFilemode = (defaultFilemode > ArchiveEntry.INVALID_MODE) ? defaultFilemode : DEFAULT_FILEMODE_VALUE;
    }

    public void setDefaultDirmode(int defaultDirmode) {
        this.defaultDirmode = (defaultDirmode > ArchiveEntry.INVALID_MODE) ? defaultDirmode : DEFAULT_DIRMODE_VALUE;
    }
}
