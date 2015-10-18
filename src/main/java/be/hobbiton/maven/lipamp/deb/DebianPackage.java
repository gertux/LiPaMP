package be.hobbiton.maven.lipamp.deb;

import static be.hobbiton.maven.lipamp.common.ArchiveEntry.ArchiveEntryType.F;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.maven.plugin.MojoExecutionException;

import be.hobbiton.maven.lipamp.common.ArchiveEntry;
import be.hobbiton.maven.lipamp.common.ArchiveEntry.ArchiveEntryType;

public class DebianPackage {
    private static final String ROOTUSERNAME = "root";
    private static final String ROOTGROUPNAME = "root";
    public static final String CURRENT_DIR = "./";
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;
    protected static final int DEFAULT_DIR_MODE = Integer.parseInt("755", 8);
    protected static final int DEFAULT_FILE_MODE = Integer.parseInt("644", 8);
    protected static final byte[] DEBIAN_BINARY_2_0 = "2.0\n".getBytes();
    private Collection<ArchiveEntry> controlFiles;
    private Collection<ArchiveEntry> dataFiles;

    public DebianPackage(Collection<File> controlFiles, Collection<ArchiveEntry> dataFiles) {
        setControlFiles(controlFiles);
        setDataFiles(dataFiles);
    }

    private final void setControlFiles(Collection<File> controlFiles) {
        if (controlFiles != null && !controlFiles.isEmpty()) {
            this.controlFiles = new ArrayList<ArchiveEntry>();
            for (File controlFile : controlFiles) {
                if (controlFile.isFile()) {
                    this.controlFiles.add(new ArchiveEntry(CURRENT_DIR + controlFile.getName(), controlFile,
                            ROOTUSERNAME, ROOTGROUPNAME, DEFAULT_FILE_MODE, F));
                } else {
                    throw new IllegalArgumentException("Invalid control file: " + controlFile.getName());
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid control files");
        }
    }

    private final void setDataFiles(Collection<ArchiveEntry> dataFiles) {
        if (dataFiles != null && !dataFiles.isEmpty()) {
            this.dataFiles = dataFiles;
        } else {
            throw new IllegalArgumentException("Invalid data files");
        }
    }

    public void write(File outputFile) throws DebianPackageException {
        ArArchiveOutputStream debianArchiveOutputStream = getDebianArchiveOutputStream(outputFile);
        File controlFile = writeControl(debianArchiveOutputStream);
        File dataFile = writeData(debianArchiveOutputStream);
        writeDebianArchive(debianArchiveOutputStream, controlFile, dataFile);
    }

    private ArArchiveOutputStream getDebianArchiveOutputStream(File outputFile) throws DebianPackageException {
        try {
            return new ArArchiveOutputStream(new FileOutputStream(outputFile));
        } catch (FileNotFoundException e) {
            throw new DebianPackageException("Cannot create Debian package file " + outputFile.getPath(), e);
        }
    }

    private void writeDebianArchive(ArArchiveOutputStream debianArchiveOutputStream, File controlFile, File dataFile)
            throws DebianPackageException {
        try {
            ArArchiveEntry debianBinaryArArchiveEntry = new ArArchiveEntry("debian-binary", 4);
            debianArchiveOutputStream.putArchiveEntry(debianBinaryArArchiveEntry);
            debianArchiveOutputStream.write(DEBIAN_BINARY_2_0);
            debianArchiveOutputStream.closeArchiveEntry();
            ArArchiveEntry controlFileArArchiveEntry = new ArArchiveEntry("control.tar.gz", controlFile.length());
            debianArchiveOutputStream.putArchiveEntry(controlFileArArchiveEntry);
            copy(new FileInputStream(controlFile), debianArchiveOutputStream);
            debianArchiveOutputStream.closeArchiveEntry();
            controlFile.delete();
            ArArchiveEntry dataFileArArchiveEntry = new ArArchiveEntry("data.tar.gz", dataFile.length());
            debianArchiveOutputStream.putArchiveEntry(dataFileArArchiveEntry);
            copy(new FileInputStream(dataFile), debianArchiveOutputStream);
            debianArchiveOutputStream.closeArchiveEntry();
            dataFile.delete();
        } catch (IOException e) {
            throw new DebianPackageException("Cannot write debian archive", e);
        }
    }

    private File writeControl(ArArchiveOutputStream debianArchiveOutputStream) throws DebianPackageException {
        return writeCompressedArchive(debianArchiveOutputStream, this.controlFiles);
    }

    private File writeData(ArArchiveOutputStream debianArchiveOutputStream) throws DebianPackageException {
        return writeCompressedArchive(debianArchiveOutputStream, this.dataFiles);
    }

    private File writeCompressedArchive(ArArchiveOutputStream debianArchiveOutputStream,
            Collection<ArchiveEntry> archiveEntries) throws DebianPackageException {
        CompressorOutputStream gzippedOutput = null;
        TarArchiveOutputStream tarOutput = null;
        try {
            File outputFile = File.createTempFile("lipm", ".tar.gz");
            outputFile.deleteOnExit();
            gzippedOutput = new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.GZIP,
                    new FileOutputStream(outputFile));
            tarOutput = new TarArchiveOutputStream(gzippedOutput);
            for (ArchiveEntry fileEntry : archiveEntries) {
                TarArchiveEntry entry = new TarArchiveEntry(fileEntry.getName());
                if (ArchiveEntryType.F.equals(fileEntry.getType())) {
                    if (fileEntry.getFile() == null) {
                        throw new DebianPackageException(
                                "Cannot write compressed archive, missing file for " + fileEntry.getName());
                    }
                    entry.setSize(fileEntry.getFile().length());
                }
                entry.setUserName(fileEntry.getUserName());
                entry.setGroupName(fileEntry.getGroupName());
                entry.setMode(fileEntry.getMode());
                tarOutput.putArchiveEntry(entry);
                if (ArchiveEntryType.F.equals(fileEntry.getType())) {
                    copy(new FileInputStream(fileEntry.getFile()), tarOutput);
                }
                tarOutput.closeArchiveEntry();
            }
            return outputFile;
        } catch (IOException e) {
            throw new DebianPackageException("Cannot write compressed archive", e);
        } catch (CompressorException e) {
            throw new DebianPackageException("Cannot compress archive", e);
        } finally {
            if (tarOutput != null) {
                try {
                    tarOutput.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static class DebianPackageException extends MojoExecutionException {
        private static final long serialVersionUID = -6514618016610125079L;

        public DebianPackageException(String message) {
            super(message);
        }

        public DebianPackageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
