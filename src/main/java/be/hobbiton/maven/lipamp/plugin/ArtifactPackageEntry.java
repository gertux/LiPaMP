package be.hobbiton.maven.lipamp.plugin;

public class ArtifactPackageEntry {
    private String artifactId;
    private String groupId;
    private String destination;
    private String username;
    private String groupname;
    private String mode;

    public ArtifactPackageEntry() {
        super();
    }

    public ArtifactPackageEntry(String artifactId, String groupId, String destination, String username,
            String groupname, String mode) {
        super();
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.destination = destination;
        this.username = username;
        this.groupname = groupname;
        this.mode = mode;
    }

    public String getArtifactId() {
        return this.artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDestination() {
        return this.destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
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
