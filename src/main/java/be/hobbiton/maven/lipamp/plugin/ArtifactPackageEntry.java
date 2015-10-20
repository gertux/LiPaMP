package be.hobbiton.maven.lipamp.plugin;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.StringUtils;

/**
 * Artifact to be included in a package
 */
public class ArtifactPackageEntry implements Comparable<Artifact> {
    protected static String DEFAULT_TYPE = "jar";
    private String artifactId;
    private String groupId;
    private String type = DEFAULT_TYPE;
    private String destination;
    private String username;
    private String groupname;
    private String mode;

    public ArtifactPackageEntry() {
        super();
    }

    public ArtifactPackageEntry(String artifactId, String groupId, String type, String destination, String username,
            String groupname, String mode) {
        super();
        this.artifactId = artifactId;
        this.groupId = groupId;
        setType(type);
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

    public String getType() {
        return this.type;
    }

    public final void setType(String type) {
        this.type = StringUtils.isNotBlank(type) ? type : DEFAULT_TYPE;
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

    @Override
    public String toString() {
        return String.format("%s:%s:%s", this.groupId, this.artifactId, this.type);
    }

    public boolean isValid() {
        return (StringUtils.isNotBlank(this.artifactId)) && (StringUtils.isNotBlank(this.groupId))
                && (StringUtils.isNotBlank(this.type)) && (StringUtils.isNotBlank(this.destination));
    }

    @Override
    public int compareTo(Artifact a) {
        int result = this.groupId.compareTo(a.getGroupId());
        if (result == 0) {
            result = this.artifactId.compareTo(a.getArtifactId());
            if (result == 0) {
                result = this.type.compareTo(a.getType());
            }
        }
        return result;
    }
}
