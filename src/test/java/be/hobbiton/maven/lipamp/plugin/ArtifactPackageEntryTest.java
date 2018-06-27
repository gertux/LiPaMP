package be.hobbiton.maven.lipamp.plugin;

import be.hobbiton.maven.lipamp.common.LinuxPackagingException;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.junit.Test;

import static be.hobbiton.maven.lipamp.common.TestConstants.*;
import static be.hobbiton.maven.lipamp.plugin.ArtifactPackageEntry.DEFAULT_TYPE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ArtifactPackageEntryTest {
    private static final DefaultArtifact DEP_ARTIFACT = new DefaultArtifact(DEP_GROUPID, DEP_ARTIFACTID, DEP_VERSION, DEP_SCOPE, DEP_PACKAGING, null, new
            DefaultArtifactHandler());
    private static final DefaultArtifact DEP_ARTIFACT_WITH_CLASSIFIER = new DefaultArtifact(DEP_GROUPID, DEP_ARTIFACTID, DEP_VERSION, DEP_SCOPE,
            DEP_PACKAGING, DEP_CLASSIFIER, new DefaultArtifactHandler());

    @Test
    public void testIsValid() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, DEP_PACKAGING, null, DEP_DESTINATION_DIR);
        assertTrue(artifactPackageEntry.isValid());
    }

    @Test
    public void testIsValidDefaultPackage() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, null, null, DEP_DESTINATION_DIR);
        assertTrue(artifactPackageEntry.isValid());
    }

    @Test(expected = LinuxPackagingException.class)
    public void testIsValidEmptyGroupId() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, "", DEP_PACKAGING, null, DEP_DESTINATION_DIR);
        artifactPackageEntry.isValid();
    }

    @Test(expected = LinuxPackagingException.class)
    public void testIsValidNullGroupId() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, null, DEP_PACKAGING, null, DEP_DESTINATION_DIR);
        artifactPackageEntry.isValid();
    }

    @Test(expected = LinuxPackagingException.class)
    public void testIsValidEmptyArtifactId() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry("", DEP_GROUPID, DEP_PACKAGING, null, DEP_DESTINATION_DIR);
        artifactPackageEntry.isValid();
    }

    @Test(expected = LinuxPackagingException.class)
    public void testIsValidNullArtifactId() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(null, DEP_GROUPID, DEP_PACKAGING, null, DEP_DESTINATION_DIR);
        artifactPackageEntry.isValid();
    }

    @Test(expected = LinuxPackagingException.class)
    public void testIsValidEmptyDestination() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, DEP_PACKAGING, null, "");
        artifactPackageEntry.isValid();
    }

    @Test(expected = LinuxPackagingException.class)
    public void testIsValidNullDestination() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, DEP_PACKAGING, null, null);
        artifactPackageEntry.isValid();
    }

    @Test
    public void testCompareToSame() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, DEP_PACKAGING, null, DEP_DESTINATION_DIR);
        assertTrue(artifactPackageEntry.matches(DEP_ARTIFACT));
    }

    @Test
    public void testCompareToSameDefaultType() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, null, null, DEP_DESTINATION_DIR);
        assertTrue(artifactPackageEntry.matches(new DefaultArtifact(DEP_GROUPID, DEP_ARTIFACTID, DEP_VERSION, DEP_SCOPE, DEFAULT_TYPE, null, new
                DefaultArtifactHandler())));
    }

    @Test
    public void testCompareToOtherDefaultType() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, null, null, DEP_DESTINATION_DIR);
        assertFalse(artifactPackageEntry.matches(new DefaultArtifact(DEP_GROUPID, DEP_ARTIFACTID, DEP_VERSION, DEP_SCOPE, "pom", null, new
                DefaultArtifactHandler())));
    }

    @Test
    public void testCompareToOtherType() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, "pom", null, DEP_DESTINATION_DIR);
        assertFalse(artifactPackageEntry.matches(DEP_ARTIFACT));
    }

    @Test
    public void testCompareToOtherGroupId() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID + "-other", DEP_PACKAGING, null, DEP_DESTINATION_DIR);
        assertFalse(artifactPackageEntry.matches(DEP_ARTIFACT));
    }

    @Test
    public void testCompareToOtherArtifactId() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID + "-other", DEP_GROUPID, DEP_PACKAGING, null, DEP_DESTINATION_DIR);
        assertFalse(artifactPackageEntry.matches(DEP_ARTIFACT));
    }

    @Test
    public void testCompareToSameWithClassifier() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry(DEP_ARTIFACTID, DEP_GROUPID, DEP_PACKAGING, null, DEP_DESTINATION_DIR);
        assertFalse(artifactPackageEntry.matches(DEP_ARTIFACT_WITH_CLASSIFIER));
    }

    @Test
    public void testCompareWithClassifier() {
        ArtifactPackageEntry artifactPackageEntry = new ArtifactPackageEntry();
        artifactPackageEntry.setArtifactId(DEP_ARTIFACTID);
        artifactPackageEntry.setGroupId(DEP_GROUPID);
        artifactPackageEntry.setType(DEP_PACKAGING);
        artifactPackageEntry.setClassifier(DEP_CLASSIFIER);
        artifactPackageEntry.setDestination(DEP_DESTINATION_DIR);
        artifactPackageEntry.setUsername(CUSTOM_USERNAME);
        artifactPackageEntry.setGroupname(CUSTOM_GROUPNAME);
        artifactPackageEntry.setModeValue(CUSTOM_FILE_MODE);
        assertTrue(artifactPackageEntry.matches(DEP_ARTIFACT_WITH_CLASSIFIER));
    }
}
