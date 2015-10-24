package be.hobbiton.maven.lipamp.plugin;

public abstract class Attributable {
    private String username;
    private String groupname;
    private String mode;

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

    public final void setMode(String mode) {
        this.mode = mode;
    }
}
