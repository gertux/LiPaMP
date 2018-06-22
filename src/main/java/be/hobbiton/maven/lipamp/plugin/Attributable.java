package be.hobbiton.maven.lipamp.plugin;

import be.hobbiton.maven.lipamp.common.ArchiveEntry;

public abstract class Attributable {
    private String username;
    private String groupname;
    private String mode;
    private int modeValue;

    public final String getUsername() {
        return this.username;
    }

    public final void setUsername(String username) {
        this.username = username;
    }

    public final String getGroupname() {
        return this.groupname;
    }

    public final void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public final String getMode() {
        return this.mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
        this.modeValue = ArchiveEntry.fromModeString(mode);
    }

    public int getModeValue() {
        return modeValue;
    }

    public void setModeValue(int modeValue) {
        this.modeValue = modeValue;
        this.mode = ArchiveEntry.fromMode(modeValue);
    }
}
