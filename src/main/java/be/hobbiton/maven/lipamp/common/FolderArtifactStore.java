/* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
   distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

   Copyright 2019 Gert Dewit <gert@hobbiton.be>
*/
package be.hobbiton.maven.lipamp.common;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FolderArtifactStore implements ArtifactStore {
    private Path baseDir;
    private String username;
    private String groupname;
    private int fileMode;
    private int dirMode;

    public FolderArtifactStore(Path baseDir, String username, String groupname, int fileMode, int dirMode) {
        this.baseDir = baseDir;
        this.username = username;
        this.groupname = groupname;
        this.fileMode = fileMode;
        this.dirMode = dirMode;
    }

    @Override
    public void save(Artifact artifact) {
        Path artifactPath = getBaseDir().resolve(toPath(artifact));
        try {
            Files.createDirectories(artifactPath.getParent());
            FileUtils.copyFileIfModified(artifact.getFile(), artifactPath.toFile());
        } catch (IOException e) {
            throw new LinuxPackagingException("Cannot copy artifact", e);
        }
    }

    @Override
    public Stream<Path> toPaths(Artifact artifact) {
        Path artifactPath = getBaseDir().resolve(toPath(artifact));
        List<Path> entries = ArtifactStore.getParentEntries(artifactPath.getParent());
        entries.add(artifactPath);
        return entries.stream();
    }

    @Override
    public Stream<ArchiveEntry> toArchiveEntries(Artifact artifact) {
        Path artifactPath = getBaseDir().resolve(toPath(artifact));
        List<ArchiveEntry> entries = ArtifactStore.getParentEntries(artifactPath.getParent()).
                stream().
                map(d -> new DirectoryArchiveEntry(d.toString(), this.username, this.groupname, this.dirMode)).
                collect(Collectors.toList());
        entries.add(new FileArchiveEntry(artifactPath.toString(), artifact.getFile(), this.username, this.groupname, this.fileMode));
        return entries.stream();
    }

    Path toPath(Artifact artifact) {
        return Paths.get(ArtifactStore.toFilename(artifact));
    }

    Path getBaseDir() {
        return baseDir;
    }
}
