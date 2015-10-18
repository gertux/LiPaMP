package be.hobbiton.maven.lipamp.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Developer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

import be.hobbiton.maven.lipamp.common.ArchiveEntry;
import be.hobbiton.maven.lipamp.common.ArchiveEntry.ArchiveEntryType;
import be.hobbiton.maven.lipamp.common.DirectoryArchiveEntry;
import be.hobbiton.maven.lipamp.common.FileArchiveEntry;
import be.hobbiton.maven.lipamp.deb.DebianControl;
import be.hobbiton.maven.lipamp.deb.DebianPackage;


@Mojo(name = "makedeb", requiresProject = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class DebianPackageMojo extends AbstractMojo {
    private static final String CURRENT_PATH = ".";
    /** As the targets for this mojo are primary Java apps, the package is by default architecture independent */
    protected static final String DEFAULT_ARCHITECTURE = "all";
    private static final String SLASH = "/";
    protected static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";
    protected static final String DEFAULT_USERNAME = "root";
    protected static final String DEFAULT_GROUPNAME = "root";
    protected static final String DEFAULT_FILEMODE = "0644";
    protected static final String DEFAULT_DIRMODE = "0755";

    @Parameter(required = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(defaultValue = DEFAULT_USERNAME)
    private String defaultUsername;

    @Parameter(defaultValue = DEFAULT_GROUPNAME)
    private String defaultGroupname;

    @Parameter(defaultValue = DEFAULT_FILEMODE)
    private String defaultFileMode;
    private Integer defaultFileModeValue;

    @Parameter(defaultValue = DEFAULT_DIRMODE)
    private String defaultDirectoryMode;
    private Integer defaultDirectoryModeValue;

    @Parameter
    private ArtifactPackageEntry[] artifacts;

    @Parameter
    private FolderEntry[] folders;

    @Parameter(defaultValue = "${project.name}")
    private String descriptionSynopsis;

    @Parameter(defaultValue = "${project.description}")
    private String description;

    @Parameter
    private String maintainer;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File packageBasedir = new File(this.project.getBasedir(), "src/main/deb");
        if (packageBasedir.isDirectory()) {
            List<ArchiveEntry> dataFiles = new ArrayList<ArchiveEntry>();
            List<File> controlFiles = new ArrayList<File>();
            findFiles(packageBasedir, dataFiles, controlFiles);
            DebianPackage debianPackage = new DebianPackage(controlFiles, dataFiles);
            debianPackage.write(getPackageFile());
        } else {
            throw new MojoFailureException("Missing package base directory");
        }
    }

    protected File getPackageFile() throws MojoExecutionException {
        return new File(getValidOutputDir(), this.project.getArtifactId() + "-" + getVersion() + ".deb");
    }

    protected String getVersion() {
        if (this.project.getVersion().endsWith(SNAPSHOT_SUFFIX)) {
            SimpleDateFormat format = new SimpleDateFormat(this.project.getVersion().substring(0,
                    this.project.getVersion().length() - SNAPSHOT_SUFFIX.length()) + "-yyyyMMddHHmmss");
            return format.format(new Date());
        }
        return this.project.getVersion();
    }

    protected String getMaintainer() {
        if (StringUtils.isNotBlank(this.maintainer)) {
            return this.maintainer;
        }
        return getMaintainerFromModel();
    }

    private String getMaintainerFromModel() {
        if (this.project.getDevelopers() != null && !this.project.getDevelopers().isEmpty()) {
            Developer dev = (Developer) this.project.getDevelopers().get(0);
            StringBuilder maintainer = new StringBuilder();
            if (StringUtils.isNotBlank(dev.getName())) {
                maintainer.append(dev.getName().trim());
            }
            if (StringUtils.isNotBlank(dev.getEmail())) {
                if (maintainer.length() > 0) {
                    maintainer.append(" ");
                }
                maintainer.append("<").append(dev.getEmail()).append(">");
            }
            if (maintainer.length() > 0) {
                return maintainer.toString();
            }
        }
        return System.getProperty("user.name");
    }

    private void findFiles(File packageBasedir, Collection<ArchiveEntry> dataFiles, Collection<File> controlFiles)
            throws MojoExecutionException, MojoFailureException {
        boolean haveControl = false;
        Map<String, ArchiveEntry> dataPaths = new HashMap<String, ArchiveEntry>();
        for (File file : packageBasedir.listFiles()) {
            if ("DEBIAN".equals(file.getName())) {
                // add control files
                for (File controlFile : file.listFiles()) {
                    getLog().debug("Adding control " + controlFile.getAbsolutePath());
                    if ("control".equals(controlFile.getName())) {
                        haveControl = true;
                    }
                    controlFiles.add(controlFile);
                }
            } else {
                addDataFile(dataFiles, file, DebianPackage.CURRENT_DIR, dataPaths);
            }
        }
        if (!haveControl) {
            controlFiles.add(generateControlFile());
        }
        if (this.artifacts != null && this.artifacts.length > 0) {
            for (ArtifactPackageEntry artifactEntry : this.artifacts) {
                if (StringUtils.isNotBlank(artifactEntry.getDestination())) {
                    Artifact depArtifact = getDependentArtifact(artifactEntry);
                    if (artifactEntry.getDestination().endsWith("/")) {
                        artifactEntry.setDestination(artifactEntry.getDestination() + depArtifact.getArtifactId() + "."
                                + depArtifact.getType());
                    }
                    File destFile = new File(cleanPath(artifactEntry.getDestination()));
                    FolderEntry parentFolder = new FolderEntry(destFile.getParent(), null, null, null);
                    addFolder(dataFiles, parentFolder, dataPaths);
                    FileArchiveEntry fileEntry = new FileArchiveEntry(destFile.getPath(), depArtifact.getFile(),
                            getUsername(artifactEntry.getUsername()), getGroupname(artifactEntry.getGroupname()),
                            getMode(artifactEntry.getMode(), destFile.getPath()));
                    dataFiles.add(fileEntry);
                    dataPaths.put(fileEntry.getName(), fileEntry);
                } else {
                    throw new MojoFailureException("Invalid artifact destination specification");
                }
            }
        }
        if (this.folders != null && this.folders.length > 0) {
            for (FolderEntry folder : this.folders) {
                String path = folder.getPath();
                if (StringUtils.isNotBlank(path)) {
                    folder.setPath(cleanPath(folder.getPath()));
                    addFolder(dataFiles, folder, dataPaths);
                }
            }
        }
    }

    private Artifact getDependentArtifact(ArtifactPackageEntry artifactEntry) throws MojoFailureException {
        if (StringUtils.isNotBlank(artifactEntry.getArtifactId())
                && StringUtils.isNotBlank(artifactEntry.getGroupId())) {
            if (this.project.getDependencyArtifacts() != null) {
                for (Object depObj : this.project.getDependencyArtifacts()) {
                    Artifact depArtifact = (Artifact) depObj;
                    if (artifactEntry.getArtifactId().equals(depArtifact.getArtifactId())
                            && artifactEntry.getGroupId().equals(depArtifact.getGroupId())) {
                        return depArtifact;
                    }
                }
            }
        } else {
            throw new MojoFailureException("Invalid artifact specification");
        }
        throw new MojoFailureException(
                String.format("Artifact %s:%s not found", artifactEntry.getGroupId(), artifactEntry.getArtifactId()));
    }

    private String cleanPath(String path) {
        String newPath = path.trim();
        if (newPath.endsWith("/")) {
            newPath = path.substring(0, path.length() - 1);
        }
        if (newPath.startsWith("/")) {
            newPath = CURRENT_PATH + newPath;
        } else if (!newPath.startsWith(DebianPackage.CURRENT_DIR)) {
            newPath = DebianPackage.CURRENT_DIR + newPath;
        }
        return newPath;
    }

    private void addFolder(Collection<ArchiveEntry> dataFiles, FolderEntry folder, Map<String, ArchiveEntry> dataPaths)
            throws MojoFailureException {
        File folderFile = new File(folder.getPath());
        File parent = null;
        if ((parent = folderFile.getParentFile()) != null) {
            if (!CURRENT_PATH.equals(parent.getPath())) {
                addFolder(dataFiles, new FolderEntry(parent.getPath(), this.defaultUsername, this.defaultGroupname,
                        this.defaultDirectoryMode), dataPaths);
            }
        }
        ArchiveEntry archiveEntry = dataPaths.get(folder.getPath() + "/");
        if (archiveEntry != null) {
            if (ArchiveEntryType.D.equals(archiveEntry.getType())) {
                if (StringUtils.isNotBlank(folder.getUsername())) {
                    getLog().debug(
                            String.format("Changing username to %s for %s", folder.getUsername(), folder.getPath()));
                    archiveEntry.setUserName(folder.getUsername().trim());
                }
                if (StringUtils.isNotBlank(folder.getGroupname())) {
                    getLog().debug(
                            String.format("Changing groupname to %s for %s", folder.getGroupname(), folder.getPath()));
                    archiveEntry.setGroupName(folder.getGroupname().trim());
                }
                if (StringUtils.isNotBlank(folder.getMode())) {
                    int mode = getMode(folder.getMode(), folder.getPath());
                    getLog().debug(String.format("Changing mode to %04o for %s", mode, folder.getPath()));
                    archiveEntry.setMode(mode);
                }
            } else {
                throw new MojoFailureException(
                        String.format("Path \"%s\" already exists, but is not a folder!", folder.getPath()));
            }
        } else {
            DirectoryArchiveEntry directoryArchiveEntry = new DirectoryArchiveEntry(folder.getPath(),
                    getUsername(folder.getUsername()), getGroupname(folder.getGroupname()),
                    getMode(folder.getMode(), folder.getPath()));
            dataFiles.add(directoryArchiveEntry);
            dataPaths.put(directoryArchiveEntry.getName(), directoryArchiveEntry);
        }
    }

    private String getUsername(String username) {
        return (StringUtils.isBlank(username) ? this.defaultUsername : username.trim());
    }

    private String getGroupname(String groupname) {
        return (StringUtils.isBlank(groupname) ? this.defaultGroupname : groupname.trim());
    }

    private int getMode(String mode, String path) throws MojoFailureException {
        if (StringUtils.isBlank(mode)) {
            return getDefaultDirectoryMode();
        }
        try {
            return Integer.parseInt(mode, 8);
        } catch (NumberFormatException e) {
            throw new MojoFailureException(
                    String.format("Path \"%s\" is configured with an invalid mode %s", path, mode));
        }
    }

    private File generateControlFile() throws MojoExecutionException {
        DebianControl control = new DebianControl();
        control.setPackageName(this.project.getArtifactId());
        control.setVersion(getVersion());
        control.setArchitecture(DEFAULT_ARCHITECTURE);
        control.setMaintainer(getMaintainer());
        control.setDescriptionSynopsis(this.descriptionSynopsis);
        control.setDescription(this.description);
        File controlFile = new File(getValidOutputDir(), "control");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(controlFile);
            control.write(fos);
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Unable to create control file", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    getLog().error(e);
                }
            }
        }
        return controlFile;
    }

    private File getValidOutputDir() throws MojoExecutionException {
        File outputDir = new File(this.project.getBuild().getDirectory());
        if (!outputDir.isDirectory()) {
            if (!outputDir.mkdirs()) {
                throw new MojoExecutionException("Unable to create output directory: " + outputDir.getAbsolutePath());
            }
        }
        return outputDir;
    }

    private void addDataFile(Collection<ArchiveEntry> dataFiles, File datafile, String prefix,
            Map<String, ArchiveEntry> dataPaths) {
        getLog().debug("Adding data " + datafile.getAbsolutePath());
        if (datafile.isDirectory()) {
            String name = prefix + datafile.getName() + SLASH;
            DirectoryArchiveEntry dirEntry = new DirectoryArchiveEntry(name, this.defaultUsername,
                    this.defaultGroupname, getDefaultDirectoryMode());
            dataFiles.add(dirEntry);
            dataPaths.put(dirEntry.getName(), dirEntry);
            for (File nestedDataFile : datafile.listFiles()) {
                addDataFile(dataFiles, nestedDataFile, name, dataPaths);
            }
        } else {
            String name = prefix + datafile.getName();
            FileArchiveEntry fileEntry = new FileArchiveEntry(name, datafile, this.defaultUsername,
                    this.defaultGroupname, getDefaultFileMode());
            dataFiles.add(fileEntry);
            dataPaths.put(fileEntry.getName(), fileEntry);
        }
    }

    protected int getDefaultDirectoryMode() {
        if (this.defaultDirectoryModeValue == null) {
            try {
                this.defaultDirectoryModeValue = Integer.parseInt(this.defaultDirectoryMode, 8);
            } catch (NumberFormatException e) {
                this.defaultDirectoryModeValue = Integer.parseInt(DEFAULT_DIRMODE, 8);
            }
        }
        return this.defaultDirectoryModeValue;
    }

    protected int getDefaultFileMode() {
        if (this.defaultFileModeValue == null) {
            try {
                this.defaultFileModeValue = Integer.parseInt(this.defaultFileMode, 8);
            } catch (NumberFormatException e) {
                this.defaultFileModeValue = Integer.parseInt(DEFAULT_FILEMODE, 8);
            }
        }
        return this.defaultFileModeValue;
    }

    protected void setProject(MavenProject project) {
        this.project = project;
    }

    protected void setDefaultUsername(String defaultUsername) {
        this.defaultUsername = defaultUsername;
    }

    protected void setDefaultGroupname(String defaultGroupname) {
        this.defaultGroupname = defaultGroupname;
    }

    protected void setDefaultFileMode(String defaultFileMode) {
        this.defaultFileMode = defaultFileMode;
    }

    protected void setDefaultDirectoryMode(String defaultDirectoryMode) {
        this.defaultDirectoryMode = defaultDirectoryMode;
    }

    protected void setArtifacts(ArtifactPackageEntry[] artifacts) {
        this.artifacts = artifacts;
    }

    protected void setDescriptionSynopsis(String descriptionSynopsis) {
        this.descriptionSynopsis = descriptionSynopsis;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    protected void setFolders(FolderEntry[] folders) {
        this.folders = folders;
    }
}
