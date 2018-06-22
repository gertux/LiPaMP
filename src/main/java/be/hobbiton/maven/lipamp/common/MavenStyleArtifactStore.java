package be.hobbiton.maven.lipamp.common;

import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static be.hobbiton.maven.lipamp.common.Constants.GROUP_SEPARATOR;

/**
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 */
public class MavenStyleArtifactStore extends FolderArtifactStore {

    public MavenStyleArtifactStore(Path baseDir, String username, String groupname, int fileMode, int dirMode) {
        super(baseDir, username, groupname, fileMode, dirMode);
    }

    @Override
    Path toPath(Artifact artifact) {
        return Paths.get(artifact.getGroupId().replace(GROUP_SEPARATOR, File.separator.charAt(0)), artifact.getArtifactId(), artifact.getBaseVersion(),
                ArtifactStore.toFilename(artifact));
    }
}
