/* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
   distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

   Copyright 2019 Gert Dewit <gert@hobbiton.be>
*/
package be.hobbiton.maven.lipamp.plugin;

import be.hobbiton.maven.lipamp.common.LinuxPackagingException;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.StringUtils;

/**
 * Artifact to be included in a package
 */
public class ArtifactPackageEntry extends Attributable {
    static final String DEFAULT_TYPE = "jar";
    private String artifactId;
    private String groupId;
    private String type = DEFAULT_TYPE;
    private String destination;
    private String classifier;

    public ArtifactPackageEntry() {
        super();
    }

    public ArtifactPackageEntry(String artifactId, String groupId, String type, String classifier, String destination) {
        super();
        this.artifactId = artifactId;
        this.groupId = groupId;
        setType(type);
        this.classifier = classifier;
        this.destination = destination;
    }

    public void setAttributes(String username, String groupname, String mode) {
        setUsername(username);
        setGroupname(groupname);
        setMode(mode);
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

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
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

    @Override
    public String toString() {
        return String.format("%s:%s:%s", this.groupId, this.artifactId, this.type);
    }

    public boolean isValid() {
        if ((StringUtils.isNotBlank(this.artifactId)) && (StringUtils.isNotBlank(this.groupId)) && (StringUtils.isNotBlank(this.type)) && (StringUtils
                .isNotBlank(this.destination))) {
            return true;
        }
        throw new LinuxPackagingException(String.format("Invalid Artifact specification %s", this.toString()));
    }

    public boolean matches(Artifact a) {
        int result = this.groupId.compareTo(a.getGroupId());
        if (result == 0) {
            result = this.artifactId.compareTo(a.getArtifactId());
            if (result == 0) {
                result = this.type.compareTo(a.getType());
                if (result == 0) {
                    if (this.classifier != null) {
                        result = this.classifier.compareTo(a.getClassifier());
                    } else if (a.getClassifier() != null) {
                        result = -1;
                    }
                }
            }
        }
        return result == 0;
    }
}
