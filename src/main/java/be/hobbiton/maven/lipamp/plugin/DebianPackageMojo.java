package be.hobbiton.maven.lipamp.plugin;

import static be.hobbiton.maven.lipamp.common.ArchiveEntryCollector.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
import be.hobbiton.maven.lipamp.common.ArchiveEntryCollector;
import be.hobbiton.maven.lipamp.common.DirectoryArchiveEntry;
import be.hobbiton.maven.lipamp.common.FileArchiveEntry;
import be.hobbiton.maven.lipamp.deb.DebianControl;
import be.hobbiton.maven.lipamp.deb.DebianPackage;

/**
 * Create a Debian package.
 * <p>
 * Binds to the package lifecycle phase for artifacts of type deb
 *
 * @since 1.0.0
 */
@Mojo(name = "makedeb", requiresProject = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class DebianPackageMojo extends AbstractMojo {
    private static final String CURRENT_PATH = ".";
    /** As the targets for this mojo are primary Java apps, the package is by default architecture independent */
    protected static final String DEFAULT_ARCHITECTURE = "all";
    protected static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    /**
     * The maven project
     *
     * @since 1.0.0
     */
    @Parameter(required = true, defaultValue = "${project}")
    private MavenProject project;

    /**
     * The username to use for files and folders when there's no explicit username set
     *
     * @since 1.0.0
     */
    @Parameter(defaultValue = DEFAULT_USERNAME)
    private String defaultUsername;

    /**
     * The group name to use for files and folders when there's no explicit group name set
     *
     * @since 1.0.0
     */
    @Parameter(defaultValue = DEFAULT_GROUPNAME)
    private String defaultGroupname;

    /**
     * The mode to use for files when there's no explicit mode set. Specified using octal notation.
     */
    @Parameter(defaultValue = DEFAULT_FILEMODE)
    private String defaultFileMode;
    private Integer defaultFileModeValue;

    /**
     * The mode to use for folders when there's no explicit mode set. Specified using octal notation.
     *
     * @since 1.0.0
     */
    @Parameter(defaultValue = DEFAULT_DIRMODE)
    private String defaultDirectoryMode;
    private Integer defaultDirectoryModeValue;

    /**
     * The dependent artifacts that should be packaged.
     *
     * <pre>
     * &lt;artifacts>
     *   &lt;artifact>
     *     &lt;groupId>be.hobbiton.app</groupId>
     *     &lt;artifactId>hiapp</artifactId>
     *     &lt;type>war</type>
     *     &lt;destination>/opt/hiapp/</destination>
     *   &lt;/artifact>
     * &lt;/artifacts>
     * </pre>
     *
     * type is optional, default value = jar
     *
     * @since 1.0.0
     */
    @Parameter
    private ArtifactPackageEntry[] artifacts;

    /**
     * The folders that should be created and packaged.
     *
     * <pre>
     * &lt;folders>
     *   &lt;folder>
     *     &lt;path>/var/log/hiapp/</path>
     *     &lt;username>hiuser</username>
     *     &lt;groupname>wheel</groupname>
     *     &lt;mode>0700</mode>
     *   &lt;/folder>
     * &lt;/folders>
     * </pre>
     *
     * username is optional, default value = root<br>
     * groupname is optional, default value = root<br>
     * mode is optional, default value = 0755
     *
     * @since 1.0.0
     */
    @Parameter
    private FolderEntry[] folders;

    /**
     * Change file and folder attributes using an Ant or Regex style file matcher
     *
     * <pre>
     * &lt;folders>
     *   &lt;folder>
     *     &lt;expression>/var/log/hiapp/</expression>
     *     &lt;username>hiuser</username>
     *     &lt;groupname>wheel</groupname>
     *     &lt;mode>0700</mode>
     *   &lt;/folder>
     * &lt;/folders>
     * </pre>
     *
     * username is optional, default value = root<br>
     * groupname is optional, default value = root<br>
     * mode is optional, default value = 0755<br>
     * one of username, groupname or mode should be specified
     *
     * @since 1.0.0
     */
    @Parameter
    private AttributeSelector[] attributes;

    /**
     * The short description
     *
     * @since 1.0.0
     */
    @Parameter(defaultValue = "${project.name}")
    private String descriptionSynopsis;

    /**
     * The extended description
     *
     * @since 1.0.0
     */
    @Parameter(defaultValue = "${project.description}")
    private String description;

    /**
     * The maintainer information, if not specified the first developer from the developer list is used or when that
     * fails the value of the user.name system property
     *
     * @since 1.0.0
     */
    @Parameter
    private String maintainer;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File packageBasedir = new File(this.project.getBasedir(), "src/main/deb");
        if (packageBasedir.isDirectory()) {
            ArchiveEntryCollector dataFilesCollector = new ArchiveEntryCollector();
            List<File> controlFiles = new ArrayList<File>();
            findFiles(packageBasedir, dataFilesCollector, controlFiles);
            DebianPackage debianPackage = new DebianPackage(controlFiles, dataFilesCollector.getEntries());
            debianPackage.write(getPackageFile());
        } else {
            throw new MojoFailureException("Missing package base directory");
        }
    }

    protected File getPackageFile() throws MojoExecutionException {
        File packageFile = new File(getValidOutputDir(), this.project.getArtifactId() + "-" + getVersion() + ".deb");
        getLog().info("Writing Debian package file to: " + packageFile.getAbsolutePath());
        return packageFile;
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

    private void findFiles(File packageBasedir, ArchiveEntryCollector dataFilesCollector, Collection<File> controlFiles)
            throws MojoExecutionException, MojoFailureException {
        boolean haveControl = false;
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
                addDataFile(dataFilesCollector, file, "/");
            }
        }
        if (!haveControl) {
            controlFiles.add(generateControlFile());
        }
        if (this.artifacts != null && this.artifacts.length > 0) {
            for (ArtifactPackageEntry artifactEntry : this.artifacts) {
                if (StringUtils.isNotBlank(artifactEntry.getDestination())) {
                    Artifact depArtifact = getDependentArtifact(artifactEntry);
                    File destFile = null;
                    if (artifactEntry.getDestination().endsWith("/")) {
                        destFile = new File(cleanPath(artifactEntry.getDestination() + depArtifact.getArtifactId() + "."
                                + depArtifact.getType()));
                    } else {
                        destFile = new File(cleanPath(artifactEntry.getDestination()));
                    }
                    DirectoryArchiveEntry parentFolder = new DirectoryArchiveEntry(destFile.getParent(), null, null,
                            ArchiveEntry.INVALID_MODE);
                    dataFilesCollector.add(parentFolder);
                    FileArchiveEntry fileEntry = new FileArchiveEntry(destFile.getPath(), depArtifact.getFile(),
                            artifactEntry.getUsername(), artifactEntry.getGroupname(),
                            getMode(artifactEntry.getMode(), destFile.getPath()));
                    dataFilesCollector.add(fileEntry);
                } else {
                    throw new MojoFailureException("Invalid artifact destination specification");
                }
            }
        }
        if (this.folders != null && this.folders.length > 0) {
            for (FolderEntry folder : this.folders) {
                if (folder.isValid()) {
                    DirectoryArchiveEntry parentFolder = new DirectoryArchiveEntry(folder.getPath(),
                            folder.getUsername(), folder.getGroupname(), getMode(folder.getMode(), folder.getPath()));
                    dataFilesCollector.add(parentFolder);
                } else {
                    throw new MojoFailureException("Invalid folder specification " + folder.toString());
                }
            }
        }
        if (this.attributes != null && this.attributes.length > 0) {
            for (AttributeSelector attributeSelector : this.attributes) {
                if (attributeSelector.isValid()) {
                    dataFilesCollector.applyAttributes(attributeSelector.getExpression(),
                            attributeSelector.getUsername(), attributeSelector.getGroupname(),
                            getMode(attributeSelector.getMode(), attributeSelector.getExpression()));
                } else {
                    throw new MojoFailureException("Invalid attributes specification " + attributeSelector.toString());
                }
            }
        }
    }

    private Artifact getDependentArtifact(ArtifactPackageEntry artifactEntry) throws MojoFailureException {
        if (artifactEntry.isValid()) {
            if (this.project.getDependencyArtifacts() != null) {
                for (Object depObj : this.project.getDependencyArtifacts()) {
                    Artifact depArtifact = (Artifact) depObj;
                    if (artifactEntry.compareTo(depArtifact) == 0) {
                        return depArtifact;
                    }
                }
            }
        } else {
            throw new MojoFailureException("Invalid artifact specification");
        }
        throw new MojoFailureException(String.format("Artifact %s not found", artifactEntry.toString()));
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

    private int getMode(String mode, String path) throws MojoFailureException {
        if (StringUtils.isBlank(mode)) {
            return ArchiveEntry.INVALID_MODE;
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

    private void addDataFile(ArchiveEntryCollector dataFilesCollector, File datafile, String prefix) {
        getLog().debug("Adding data " + datafile.getAbsolutePath());
        String name = prefix + datafile.getName();
        if (datafile.isDirectory()) {
            DirectoryArchiveEntry dirEntry = new DirectoryArchiveEntry(name, this.defaultUsername,
                    this.defaultGroupname, getDefaultDirectoryMode());
            dataFilesCollector.add(dirEntry);
            for (File nestedDataFile : datafile.listFiles()) {
                addDataFile(dataFilesCollector, nestedDataFile, name + "/");
            }
        } else {
            FileArchiveEntry fileEntry = new FileArchiveEntry(name, datafile, this.defaultUsername,
                    this.defaultGroupname, getDefaultFileMode());
            dataFilesCollector.add(fileEntry);
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

    protected void setAttributes(AttributeSelector[] attributes) {
        this.attributes = attributes;
    }
}
