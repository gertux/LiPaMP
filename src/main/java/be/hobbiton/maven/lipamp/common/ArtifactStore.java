package be.hobbiton.maven.lipamp.common;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static be.hobbiton.maven.lipamp.common.Constants.ARTIFACT_SEPARATOR;
import static be.hobbiton.maven.lipamp.common.Constants.GROUP_SEPARATOR;

/**
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 */
public interface ArtifactStore {

    static String toFilename(Artifact artifact) {
        StringBuilder filename = new StringBuilder(artifact.getArtifactId());
        filename.append(ARTIFACT_SEPARATOR).append(artifact.getVersion());
        if (artifact.hasClassifier()) {
            filename.append(ARTIFACT_SEPARATOR).append(artifact.getClassifier());
        }
        ArtifactHandler artifactHandler = artifact.getArtifactHandler();
        if (StringUtils.isNotBlank(artifactHandler.getExtension())) {
            filename.append(GROUP_SEPARATOR).append(artifactHandler.getExtension());
        }
        return filename.toString();
    }

    static List<Path> getParentEntries(Path parent) {
        List<Path> archiveEntries = new ArrayList<>();
        if (parent != null) {
            Path dir = Paths.get("");
            for (Path pathElem : parent) {
                dir = dir.resolve(pathElem);
                archiveEntries.add(dir);
            }
        }
        return archiveEntries;
    }

    void save(Artifact artifact) throws IOException;

    Stream<Path> toPaths(Artifact artifact);

    Stream<ArchiveEntry> toArchiveEntries(Artifact artifact);
}

