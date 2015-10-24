package be.hobbiton.maven.lipamp.plugin;

import static be.hobbiton.maven.lipamp.common.TestConstants.*;
import static be.hobbiton.maven.lipamp.plugin.ArtifactPackageEntry.DEFAULT_TYPE;
import static org.junit.Assert.*;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.junit.Test;

public class ArtifactPackageEntryTest {
    private static final DefaultArtifact DEP_ARTIFACT = new DefaultArtifact(DEP_GROUPID, DEP_ARTIFACTID, DEP_VERSION,
            DEP_SCOPE, DEP_PACKAGING, null, new DefaultArtifactHandler());

    @Test
    public void testIsValid() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, DEP_PACKAGING,
                DEP_DESTINATION, null, null, null);
        assertTrue(artifactPackageEntry.isValid());
    }

    @Test
    public void testIsValidDefaultPackage() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, null,
                DEP_DESTINATION, null, null, null);
        assertTrue(artifactPackageEntry.isValid());
    }

    @Test
    public void testIsValidEmptyGroupId() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, "", DEP_PACKAGING,
                DEP_DESTINATION, null, null, null);
        assertFalse(artifactPackageEntry.isValid());
    }

    @Test
    public void testIsValidNullGroupId() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, null, DEP_PACKAGING,
                DEP_DESTINATION, null, null, null);
        assertFalse(artifactPackageEntry.isValid());
    }

    @Test
    public void testIsValidEmptyArtifactId() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry("", DEP_GROUPID, DEP_PACKAGING,
                DEP_DESTINATION, null, null, null);
        assertFalse(artifactPackageEntry.isValid());
    }

    @Test
    public void testIsValidNullArtifactId() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(null, DEP_GROUPID, DEP_PACKAGING,
                DEP_DESTINATION, null, null, null);
        assertFalse(artifactPackageEntry.isValid());
    }

    @Test
    public void testIsValidEmptyDestination() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, DEP_PACKAGING,
                "", null, null, null);
        assertFalse(artifactPackageEntry.isValid());
    }

    @Test
    public void testIsValidNullDestination() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, DEP_PACKAGING,
                null, null, null, null);
        assertFalse(artifactPackageEntry.isValid());
    }

    @Test
    public void testCompareToSame() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, DEP_PACKAGING,
                DEP_DESTINATION, null, null, null);
        assertTrue(artifactPackageEntry.matches(DEP_ARTIFACT));
    }

    @Test
    public void testCompareToSameDefaultType() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, null,
                DEP_DESTINATION, null, null, null);
        assertTrue(artifactPackageEntry.matches(new DefaultArtifact(DEP_GROUPID, DEP_ARTIFACTID, DEP_VERSION, DEP_SCOPE,
                DEFAULT_TYPE, null, new DefaultArtifactHandler())));
    }

    @Test
    public void testCompareToOtherDefaultType() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, null,
                DEP_DESTINATION, null, null, null);
        assertFalse(artifactPackageEntry.matches(new DefaultArtifact(DEP_GROUPID, DEP_ARTIFACTID, DEP_VERSION,
                DEP_SCOPE, "pom", null, new DefaultArtifactHandler())));
    }

    @Test
    public void testCompareToOtherType() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, "pom",
                DEP_DESTINATION, null, null, null);
        assertFalse(artifactPackageEntry.matches(DEP_ARTIFACT));
    }

    @Test
    public void testCompareToOtherGroupId() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID + "-other",
                DEP_PACKAGING, DEP_DESTINATION, null, null, null);
        assertFalse(artifactPackageEntry.matches(DEP_ARTIFACT));
    }

    @Test
    public void testCompareToOtherArtifactId() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID + "-other", DEP_GROUPID,
                DEP_PACKAGING, DEP_DESTINATION, null, null, null);
        assertFalse(artifactPackageEntry.matches(DEP_ARTIFACT));
    }
}
