package be.hobbiton.maven.lipamp.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import be.hobbiton.maven.lipamp.common.ArchiveEntry.ArchiveEntryType;

public class ArchiveEntryTest {

    @Test
    public void testModeString() {
        assertEquals("drwxr-x--x", ArchiveEntry.getModeString(ArchiveEntryType.D, 0751));
        assertEquals("-rwx--xr-x", ArchiveEntry.getModeString(ArchiveEntryType.F, 0715));
        assertEquals("drw----r--", ArchiveEntry.getModeString(ArchiveEntryType.D, 0604));
        assertEquals("----r--rw-", ArchiveEntry.getModeString(ArchiveEntryType.F, 0046));
        assertEquals("l--x-w--wx", ArchiveEntry.getModeString(ArchiveEntryType.S, 0123));
        assertEquals("-rwxrw-r-x", ArchiveEntry.getModeString(ArchiveEntryType.L, 0765));
    }

}
