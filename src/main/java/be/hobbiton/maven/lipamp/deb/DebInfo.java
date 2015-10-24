package be.hobbiton.maven.lipamp.deb;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

import be.hobbiton.maven.lipamp.common.ArchiveEntry;
import be.hobbiton.maven.lipamp.common.DirectoryArchiveEntry;
import be.hobbiton.maven.lipamp.common.FileArchiveEntry;
import be.hobbiton.maven.lipamp.deb.DebianControl.DebianControlField;

/**
 * Debian binary package handler
 * <p>
 * Doesn't aim to be complete, just to be useful for binary Java based packages
 *
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 *
 */
public class DebInfo {
    private static final String CONTROL_SUFFIX = "/" + DebianInfoFile.CONTROL.getFilename();
    private static final String CONFFILES_SUFFIX = "/" + DebianInfoFile.CONFFILES.getFilename();
    private static final String LINEFEED = "\n";
    private DebianControl control;
    private Collection<File> controlFiles;
    private Collection<ArchiveEntry> dataFiles;
    private Collection<File> conffiles;
    private Log logger;

    public DebInfo(File packageFile, Log logger) {
        super();
        this.logger = logger;
        readFile(packageFile);
    }

    public DebianControl getControl() {
        return this.control;
    }

    public Collection<File> getControlFiles() {
        return this.controlFiles;
    }

    public Collection<ArchiveEntry> getDataFiles() {
        return this.dataFiles;
    }

    public Collection<File> getConffiles() {
        return this.conffiles;
    }

    private final void readControl(InputStream input) {
        BufferedInputStream bufInput = null;
        TarArchiveInputStream tar = null;
        try {
            bufInput = new BufferedInputStream(input);
            CompressorInputStream compInput = new CompressorStreamFactory().createCompressorInputStream(bufInput);
            tar = new TarArchiveInputStream(compInput);
            TarArchiveEntry tarEntry = tar.getNextTarEntry();
            this.controlFiles = new ArrayList<File>();
            while (tarEntry != null) {
                this.controlFiles.add(new File(tarEntry.getName()));
                if (DebianInfoFile.CONTROL.getFilename().equals(tarEntry.getName())
                        || tarEntry.getName().endsWith(CONTROL_SUFFIX)) {
                    this.control = new DebianControl(tar, this.logger);
                } else if (DebianInfoFile.CONFFILES.getFilename().equals(tarEntry.getName())
                        || tarEntry.getName().endsWith(CONFFILES_SUFFIX)) {
                    readConffiles(tar);
                }
                tarEntry = tar.getNextTarEntry();
            }
        } catch (CompressorException e) {
            throw new DebianArchiveException("Unable to read Control Archive", e);
        } catch (IOException e) {
            throw new DebianArchiveException("Unable to read Control Archive entry", e);
        }
    }

    private final void readConffiles(InputStream input) {
        this.conffiles = new HashSet<File>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                String path = line.trim();
                if (StringUtils.isNotBlank(path)) {
                    this.conffiles.add(new File(path));
                }
            }
        } catch (IOException e) {
            throw new DebianArchiveException("Unable to read conffiles File", e);
        }
    }

    @SuppressWarnings("resource")
    private final void readData(InputStream input) {
        BufferedInputStream bufInput = null;
        TarArchiveInputStream tar = null;
        try {
            bufInput = new BufferedInputStream(input);
            CompressorInputStream compInput = new CompressorStreamFactory().createCompressorInputStream(bufInput);
            tar = new TarArchiveInputStream(compInput);
            TarArchiveEntry tarEntry = tar.getNextTarEntry();
            this.dataFiles = new ArrayList<ArchiveEntry>();
            while (tarEntry != null) {
                if (tarEntry.isDirectory()) {
                    this.dataFiles.add(new DirectoryArchiveEntry(tarEntry.getName(), tarEntry.getUserName(),
                            tarEntry.getGroupName(), tarEntry.getMode()));
                } else if (tarEntry.isFile()) {
                    this.dataFiles.add(new FileArchiveEntry(tarEntry.getName(), new File(tarEntry.getName()),
                            tarEntry.getUserName(), tarEntry.getGroupName(), tarEntry.getMode()));
                }
                tarEntry = tar.getNextTarEntry();
            }
        } catch (CompressorException e) {
            throw new DebianArchiveException("Unable to read Data Archive", e);
        } catch (IOException e) {
            throw new DebianArchiveException("Unable to read Data Archive entry", e);
        }
    }

    private final InputStream getInputStream(File pkgFile) {
        try {
            return new FileInputStream(pkgFile);
        } catch (FileNotFoundException e) {
            throw new DebianArchiveException("Cannot read package file", e);
        }
    }

    private final ArArchiveInputStream getArArchiveInputStream(File pkgFile) {
        try {
            return (ArArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.AR,
                    getInputStream(pkgFile));
        } catch (ArchiveException e) {
            throw new DebianArchiveException("Unable to open AR Archive", e);
        }

    }

    private final void readFile(File pkgFile) {
        ArArchiveInputStream archiveStream = getArArchiveInputStream(pkgFile);
        try {
            readDebianBinary(archiveStream);
            readControlArchive(archiveStream);
            readDataArchive(archiveStream);
        } catch (IOException e) {
            throw new DebianArchiveException("Unable to read AR Archive entry", e);
        }
    }

    private void readDataArchive(ArArchiveInputStream archiveStream) throws IOException {
        ArArchiveEntry thirdEntry = archiveStream.getNextArEntry();
        if (thirdEntry == null || !thirdEntry.getName().startsWith("data.tar")) {
            throw new DebianArchiveException("Unexpected entry, data archive missing");
        } else {
            readData(archiveStream);
        }
    }

    private void readControlArchive(ArArchiveInputStream archiveStream) throws IOException {
        ArArchiveEntry secondEntry = archiveStream.getNextArEntry();
        if (secondEntry == null || !secondEntry.getName().startsWith("control.tar")) {
            throw new DebianArchiveException("Unexpected entry, control archive missing");
        } else {
            readControl(archiveStream);
        }
    }

    private void readDebianBinary(ArArchiveInputStream archiveStream) throws IOException {
        ArArchiveEntry firstEntry = archiveStream.getNextArEntry();
        if (firstEntry == null || !"debian-binary".equals(firstEntry.getName())) {
            throw new DebianArchiveException("Unexpected entry, debian-binary missing");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(DebianControlField.PACKAGE.getFieldname()).append(": ").append(this.control.getPackageName())
        .append(LINEFEED);
        if (StringUtils.isNotBlank(this.control.getSection())) {
            sb.append(DebianControlField.SECTION.getFieldname()).append(": ").append(this.control.getSection())
            .append(LINEFEED);
        }
        if (StringUtils.isNotBlank(this.control.getPriority())) {
            sb.append(DebianControlField.PRIORITY.getFieldname()).append(": ").append(this.control.getPriority())
            .append(LINEFEED);
        }
        sb.append(DebianControlField.MAINTAINER.getFieldname()).append(": ").append(this.control.getMaintainer())
        .append(LINEFEED);
        if (this.control.getInstalledSize() > DebianControl.INVALID_SIZE) {
            sb.append(DebianControlField.INSTALLED_SIZE.getFieldname()).append(": ")
            .append(this.control.getInstalledSize()).append(LINEFEED);
        }
        sb.append(DebianControlField.VERSION.getFieldname()).append(": ").append(this.control.getVersion())
        .append(LINEFEED);
        sb.append(DebianControlField.ARCHITECTURE.getFieldname()).append(": ").append(this.control.getArchitecture())
        .append(LINEFEED);
        sb.append(DebianControlField.DESCRIPTION.getFieldname()).append(": ")
        .append(this.control.getDescriptionSynopsis()).append(LINEFEED);
        if (this.control.getDescription() != null) {
            sb.append(this.control.getDescription()).append(LINEFEED);
        }
        sb.append("\nControl files:\n");
        for (File controlFile : getControlFiles()) {
            sb.append(controlFile.getName()).append(LINEFEED);
        }
        if (this.conffiles != null && !this.conffiles.isEmpty()) {
            sb.append("\nConfiguration files:\n");
            for (File configFile : this.conffiles) {
                sb.append(configFile.getAbsolutePath()).append(LINEFEED);
            }
        }
        sb.append("\nData files:\n");
        for (ArchiveEntry dataFile : getDataFiles()) {
            sb.append(dataFile).append(LINEFEED);
        }
        return sb.toString();
    }

    public enum DebianInfoFile {
        CONTROL("control"), CONFFILES("conffiles"), PRE_INSTALL("preinst"), POST_INSTALL("postinst"), PRE_REMOVE(
                "premr"), POST_REMOVE("postrm");
        private final String filename;

        DebianInfoFile(String n) {
            this.filename = n;
        }

        public final String getFilename() {
            return this.filename;
        }
    }
}
