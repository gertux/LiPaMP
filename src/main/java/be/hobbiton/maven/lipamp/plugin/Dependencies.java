package be.hobbiton.maven.lipamp.plugin;

import be.hobbiton.maven.lipamp.common.ArtifactStore;
import be.hobbiton.maven.lipamp.common.FolderArtifactStore;
import be.hobbiton.maven.lipamp.common.LinuxPackagingException;
import be.hobbiton.maven.lipamp.common.MavenStyleArtifactStore;
import org.codehaus.plexus.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import static be.hobbiton.maven.lipamp.common.ArchiveEntry.*;
import static be.hobbiton.maven.lipamp.common.Constants.DOT;
import static be.hobbiton.maven.lipamp.common.Constants.SLASH;

/**
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 */
public class Dependencies extends Attributable {
    private String destination;
    private String type;
    private int dirModeValue;

    public Dependencies() {
    }

    public Dependencies(String destination, String type, String username, String groupname, String fileMode) {
        this.destination = destination;
        setType(type);
        setUsername(username);
        setGroupname(groupname);
        setMode(fileMode);
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getDirModeValue() {
        return dirModeValue;
    }

    public void setDirModeValue(int dirModeValue) {
        this.dirModeValue = dirModeValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArtifactStore getArtifactStore(String defaultUsername, String defaultGroupname, int defaultFileMode, int defaultDirMode) {
        if(StringUtils.isBlank(this.destination)) {
            throw new LinuxPackagingException("Invalid dependencies configuration, no destination specified");
        }
        String username = stringValueOrDefault(getUsername(), defaultUsername);
        String groupname = stringValueOrDefault(getGroupname(), defaultGroupname);
        int fileMode = modeValueOrDefault(getModeValue(), defaultFileMode);
        int dirMode = modeValueOrDefault(getDirModeValue(), defaultDirMode);
        Path repoRoot;
        if (this.destination.startsWith(SLASH)) {
            repoRoot = Paths.get(DOT, this.destination);
        } else {
            repoRoot = Paths.get(DOT, SLASH, this.destination);
        }
        if("flat".equals(this.type)) {
            return new FolderArtifactStore(repoRoot, username, groupname, fileMode, dirMode);
        }
        return new MavenStyleArtifactStore(repoRoot, username, groupname, fileMode, dirMode);
    }
}
