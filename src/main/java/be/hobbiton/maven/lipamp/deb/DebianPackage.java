package be.hobbiton.maven.lipamp.deb;

import be.hobbiton.maven.lipamp.common.*;
import be.hobbiton.maven.lipamp.common.ArchiveEntry.ArchiveEntryType;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.maven.plugin.logging.Log;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static be.hobbiton.maven.lipamp.common.ArchiveEntry.ArchiveEntryType.F;
import static be.hobbiton.maven.lipamp.common.Constants.CURRENT_DIR;
import static be.hobbiton.maven.lipamp.common.Constants.CURRENT_DIR_PATH;

public class DebianPackage implements Packager {
    static final int DEFAULT_DIR_MODE = Integer.parseInt("755", 8);
    static final int DEFAULT_FILE_MODE = Integer.parseInt("644", 8);
    private static final String ROOTUSERNAME = "root";
    private static final String ROOTGROUPNAME = "root";
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;
    private static final byte[] DEBIAN_BINARY_2_0 = "2.0\n".getBytes();
    private Collection<ArchiveEntry> controlFiles;
    private Collection<ArchiveEntry> dataFiles;
    private Log logger;

    public DebianPackage(Collection<File> controlFiles, Collection<ArchiveEntry> dataFiles, Log logger) {
        this.logger = logger;
        setControlFiles(controlFiles);
        setDataArchiveEntries(dataFiles);
    }

    public DebianPackage(Log logger, Collection<Path> controlFilePaths, Collection<ArchiveEntry> dataArchiveEntries) {
        this.logger = logger;
        setControlPaths(controlFilePaths);
        setDataArchiveEntries(dataArchiveEntries);
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

    private final void setControlPaths(Collection<Path> controlPaths) {
        if (controlPaths != null && !controlPaths.isEmpty()) {
            this.controlFiles = controlPaths.stream().filter(Files::isRegularFile).map(path -> new FileArchiveEntry(CURRENT_DIR_PATH.resolve(path.getFileName
                    ()).toString(), path.toFile(), ROOTUSERNAME, ROOTGROUPNAME, DEFAULT_FILE_MODE)).collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Invalid control files");
        }
    }

    private final void setControlFiles(Collection<File> controlFiles) {
        if (controlFiles != null && !controlFiles.isEmpty()) {
            this.controlFiles = new ArrayList<>();
            for (File controlFile : controlFiles) {
                if (controlFile.isFile()) {
                    this.controlFiles.add(new ArchiveEntry(CURRENT_DIR + controlFile.getName(), controlFile, ROOTUSERNAME, ROOTGROUPNAME, DEFAULT_FILE_MODE,
                            F));
                } else {
                    throw new IllegalArgumentException("Invalid control file: " + controlFile.getName());
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid control files");
        }
    }

    private final void setDataArchiveEntries(Collection<ArchiveEntry> dataFiles) {
        if (dataFiles != null && !dataFiles.isEmpty()) {
            this.dataFiles = dataFiles;
        } else {
            throw new IllegalArgumentException("Invalid data files");
        }
    }

    @Override
    public void write(File outputFile) {
        this.logger.debug("WRITING to " + outputFile.getAbsolutePath());
        ArArchiveOutputStream debianArchiveOutputStream = getDebianArchiveOutputStream(outputFile);
        File controlFile = writeControl();
        File dataFile = writeData();
        writeDebianArchive(debianArchiveOutputStream, controlFile, dataFile);
    }

    private ArArchiveOutputStream getDebianArchiveOutputStream(File outputFile) {
        try {
            return new ArArchiveOutputStream(new FileOutputStream(outputFile));
        } catch (FileNotFoundException e) {
            throw new DebianPackageException("Cannot create Debian package file " + outputFile.getPath(), e);
        }
    }

    private void writeDebianArchive(ArArchiveOutputStream debianArchiveOutputStream, File controlFile, File dataFile) {
        try {
            ArArchiveEntry debianBinaryArArchiveEntry = new ArArchiveEntry("debian-binary", 4);
            debianArchiveOutputStream.putArchiveEntry(debianBinaryArArchiveEntry);
            debianArchiveOutputStream.write(DEBIAN_BINARY_2_0);
            debianArchiveOutputStream.closeArchiveEntry();
            ArArchiveEntry controlFileArArchiveEntry = new ArArchiveEntry("control.tar.gz", controlFile.length());
            debianArchiveOutputStream.putArchiveEntry(controlFileArArchiveEntry);
            copy(new FileInputStream(controlFile), debianArchiveOutputStream);
            debianArchiveOutputStream.closeArchiveEntry();
            deleteTempFile(controlFile);
            ArArchiveEntry dataFileArArchiveEntry = new ArArchiveEntry("data.tar.gz", dataFile.length());
            debianArchiveOutputStream.putArchiveEntry(dataFileArArchiveEntry);
            copy(new FileInputStream(dataFile), debianArchiveOutputStream);
            debianArchiveOutputStream.closeArchiveEntry();
            deleteTempFile(dataFile);
        } catch (IOException e) {
            throw new DebianPackageException("Cannot write debian archive", e);
        }
    }

    private void deleteTempFile(File tempFile) {
        try {
            Files.delete(tempFile.toPath());
        } catch (IOException e) {
            this.logger.warn("Failed to delete temp file ".concat(tempFile.toString()), e);
        }
    }

    private File writeControl() {
        if (logger.isDebugEnabled()) {
            logger.debug("Writing control archive");
        }
        return writeCompressedArchive(this.controlFiles);
    }

    private File writeData() {
        if (logger.isDebugEnabled()) {
            logger.debug("Writing data archive");
        }
        return writeCompressedArchive(this.dataFiles);
    }

    private String getTarName(String path) {
        if (path.startsWith("/")) {
            return "." + path;
        }
        return path;
    }

    private File writeCompressedArchive(Collection<ArchiveEntry> archiveEntries) {
        File outputFile = createTempDebianFile();
        try (CompressorOutputStream gzippedOutput = new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.GZIP, new
                FileOutputStream(outputFile)); TarArchiveOutputStream tarOutput = writeTarArchive(archiveEntries, gzippedOutput)) {
            return outputFile;
        } catch (IOException e) {
            throw new DebianPackageException("Cannot write compressed archive", e);
        } catch (CompressorException e) {
            throw new DebianPackageException("Cannot compress archive", e);
        }
    }

    private File createTempDebianFile() {
        try {
            File outputFile = File.createTempFile("lipm", ".tar.gz");
            outputFile.deleteOnExit();
            return outputFile;
        } catch (IOException e) {
            throw new DebianPackageException("Cannot create temp file", e);
        }
    }

    private TarArchiveOutputStream writeTarArchive(Collection<ArchiveEntry> archiveEntries, CompressorOutputStream gzippedOutput) throws IOException {
        TarArchiveOutputStream tarOutput;
        tarOutput = new TarArchiveOutputStream(gzippedOutput);
        tarOutput.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        for (ArchiveEntry fileEntry : archiveEntries) {
            if (logger.isDebugEnabled()) {
                logger.debug("Archiving ".concat(String.valueOf(fileEntry)));
            }
            TarArchiveEntry entry;
            if (ArchiveEntryType.S.equals(fileEntry.getType())) {
                entry = new TarArchiveEntry(getTarName(fileEntry.getName()), TarConstants.LF_SYMLINK);
                entry.setLinkName(((SymbolicLinkArchiveEntry) fileEntry).getTarget());
            } else {
                entry = new TarArchiveEntry(getTarName(fileEntry.getName()));
            }
            if (ArchiveEntryType.F.equals(fileEntry.getType())) {
                checkContentsFile(fileEntry);
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
        return tarOutput;
    }

    private void checkContentsFile(ArchiveEntry fileEntry) {
        if (fileEntry.getFile() == null) {
            throw new DebianPackageException("Cannot write compressed archive, missing file for " + fileEntry.getName());
        }
    }

    public static class DebianPackageException extends LinuxPackagingException {
        private static final long serialVersionUID = -6514618016610125079L;

        public DebianPackageException(String message) {
            super(message);
        }

        public DebianPackageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
