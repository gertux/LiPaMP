package be.hobbiton.maven.lipamp.deb;

import be.hobbiton.maven.lipamp.common.*;
import be.hobbiton.maven.lipamp.deb.DebianControl.DebianControlField;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static be.hobbiton.maven.lipamp.common.Constants.INVALID_SIZE;

/**
 * Debian binary package handler
 * <p>
 * Doesn't aim to be complete, just to be useful for binary Java based packages
 *
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
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
        this.logger = logger;
        init(packageFile);
    }

    public DebInfo(Path packageFilePath, Log logger) {
        this(packageFilePath.toFile(), logger);
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

    private final void init(File packageFile) {
        try (ArArchiveInputStream archiveStream = (ArArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.AR, new
                FileInputStream(packageFile))) {
            ArArchiveEntry firstEntry = archiveStream.getNextArEntry();
            if (firstEntry == null || !"debian-binary".equals(firstEntry.getName())) {
                throw new LinuxPackagingException("Unexpected entry, debian-binary missing");
            }
            ArArchiveEntry secondEntry = archiveStream.getNextArEntry();
            if (secondEntry == null || !secondEntry.getName().startsWith("control.tar")) {
                throw new LinuxPackagingException("Unexpected entry, control archive missing");
            } else {
                readControlEntries(new CompressorStreamFactory().createCompressorInputStream(new BufferedInputStream(archiveStream)));
            }
            ArArchiveEntry thirdEntry = archiveStream.getNextArEntry();
            if (thirdEntry == null || !thirdEntry.getName().startsWith("data.tar")) {
                throw new LinuxPackagingException("Unexpected entry, data archive missing");
            } else {
                readDataEntries(new CompressorStreamFactory().createCompressorInputStream(new BufferedInputStream(archiveStream)));
            }
        } catch (IOException | CompressorException | ArchiveException e) {
            throw new LinuxPackagingException("Cannot read package file", e);
        }
    }

    private final void readDataEntries(InputStream input) {
        TarArchiveInputStream tar = null;
        try {
            tar = new TarArchiveInputStream(input);
            TarArchiveEntry tarEntry = tar.getNextTarEntry();
            this.dataFiles = new ArrayList<>();
            while (tarEntry != null) {
                if (tarEntry.isSymbolicLink()) {
                    this.dataFiles.add(new SymbolicLinkArchiveEntry(tarEntry.getName(), tarEntry.getLinkName(), tarEntry.getUserName(), tarEntry.getGroupName
                            (), tarEntry.getMode()));
                } else if (tarEntry.isDirectory()) {
                    this.dataFiles.add(new DirectoryArchiveEntry(tarEntry.getName(), tarEntry.getUserName(), tarEntry.getGroupName(), tarEntry.getMode()));
                } else if (tarEntry.isFile()) {
                    FileArchiveEntry fileEntry = new FileArchiveEntry(tarEntry.getName(), new File(tarEntry.getName()), tarEntry.getUserName(), tarEntry
                            .getGroupName(), tarEntry.getMode());
                    fileEntry.setSize(tarEntry.getSize());
                    this.dataFiles.add(fileEntry);
                }
                tarEntry = tar.getNextTarEntry();
            }
        } catch (IOException e) {
            throw new LinuxPackagingException("Unable to read Data Archive entry", e);
        }
    }


    private final void readControlEntries(InputStream input) {
        TarArchiveInputStream tar = null;
        try {
            tar = new TarArchiveInputStream(input);
            TarArchiveEntry tarEntry = tar.getNextTarEntry();
            this.controlFiles = new ArrayList<>();
            while (tarEntry != null) {
                this.controlFiles.add(new File(tarEntry.getName()));
                if (DebianInfoFile.CONTROL.getFilename().equals(tarEntry.getName()) || tarEntry.getName().endsWith(CONTROL_SUFFIX)) {
                    this.control = new DebianControl(tar, this.logger);
                } else if (DebianInfoFile.CONFFILES.getFilename().equals(tarEntry.getName()) || tarEntry.getName().endsWith(CONFFILES_SUFFIX)) {
                    readConffiles(tar);
                }
                tarEntry = tar.getNextTarEntry();
            }
        } catch (IOException e) {
            throw new LinuxPackagingException("Unable to read Control Archive entry", e);
        }
    }

    private final void readConffiles(InputStream input) {
        this.conffiles = new HashSet<>();
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
            throw new LinuxPackagingException("Unable to read conffiles File", e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(LINEFEED).append(DebianControlField.PACKAGE.getFieldname()).append(": ").append(this.control.getPackageName()).append(LINEFEED);
        if (StringUtils.isNotBlank(this.control.getSection())) {
            sb.append(DebianControlField.SECTION.getFieldname()).append(": ").append(this.control.getSection()).append(LINEFEED);
        }
        if (StringUtils.isNotBlank(this.control.getPriority())) {
            sb.append(DebianControlField.PRIORITY.getFieldname()).append(": ").append(this.control.getPriority()).append(LINEFEED);
        }
        sb.append(DebianControlField.MAINTAINER.getFieldname()).append(": ").append(this.control.getMaintainer()).append(LINEFEED);
        if (this.control.getInstalledSize() > INVALID_SIZE) {
            sb.append(DebianControlField.INSTALLED_SIZE.getFieldname()).append(": ").append(this.control.getInstalledSize()).append(LINEFEED);
        }
        sb.append(DebianControlField.VERSION.getFieldname()).append(": ").append(this.control.getVersion()).append(LINEFEED);
        sb.append(DebianControlField.ARCHITECTURE.getFieldname()).append(": ").append(this.control.getArchitecture()).append(LINEFEED);
        sb.append(DebianControlField.DESCRIPTION.getFieldname()).append(": ").append(this.control.getDescriptionSynopsis()).append(LINEFEED);
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
        CONTROL("control"), CONFFILES("conffiles"), PRE_INSTALL("preinst"), POST_INSTALL("postinst"), PRE_REMOVE("premr"), POST_REMOVE("postrm");
        private final String filename;

        DebianInfoFile(String n) {
            this.filename = n;
        }

        public final String getFilename() {
            return this.filename;
        }
    }
}
