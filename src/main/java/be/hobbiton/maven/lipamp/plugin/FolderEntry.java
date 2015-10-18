package be.hobbiton.maven.lipamp.plugin;

public class FolderEntry {
    private String path;
    private String username;
    private String groupname;
    private String mode;

    public FolderEntry() {
        super();
    }

    public FolderEntry(String path, String username, String groupname, String mode) {
        super();
        this.path = path;
        this.username = username;
        this.groupname = groupname;
        this.mode = mode;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGroupname() {
        return this.groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getMode() {
        return this.mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
