package be.hobbiton.maven.lipamp.common;

import java.io.File;

public class FileArchiveEntry extends ArchiveEntry {

    public FileArchiveEntry(String name, File file, String userName, String groupName, int mode) {
        super(name, file, userName, groupName, mode, ArchiveEntryType.F);
        if (file != null && file.isFile()) {
            setSize(file.length());
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (getFile() != null && getFile().isFile());
    }
}