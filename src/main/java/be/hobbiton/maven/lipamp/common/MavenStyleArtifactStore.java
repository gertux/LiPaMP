/* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
   distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

   Copyright 2019 Gert Dewit <gert@hobbiton.be>
*/
package be.hobbiton.maven.lipamp.common;

import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static be.hobbiton.maven.lipamp.common.Constants.GROUP_SEPARATOR;

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
