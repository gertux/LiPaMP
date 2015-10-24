package be.hobbiton.maven.lipamp.plugin;

import org.codehaus.plexus.util.StringUtils;

public class FolderEntry extends Attributable {
    private String path;

    public FolderEntry() {
        super();
    }

    public FolderEntry(String path, String username, String groupname, String mode) {
        super();
        this.path = path;
        setUsername(username);
        setGroupname(groupname);
        setMode(mode);
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.path);
    }

    @Override
    public String toString() {
        return String.format("path=%s u=%s g=%s m=%s", this.path, getUsername(), getGroupname(), getMode());
    }
}
