package be.hobbiton.maven.lipamp.plugin;

import static be.hobbiton.maven.lipamp.common.ArchiveEntryCollector.*;
import static be.hobbiton.maven.lipamp.deb.DebInfo.DebianInfoFile.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public static final String DEBIAN_PACKAGING_TYPE = "deb";
    public static final String DEBIAN_RESOURCES_DIRNAME = "deb";
    public static final String DEBIAN_FILE_EXTENSION = ".deb";
    public static final String CONFFILES_DIRNAME = "DEBIAN";
    private static final String CURRENT_PATH = DOT;
    /** As the targets for this mojo are primary Java apps, the package is by default architecture independent */
    protected static final String DEFAULT_ARCHITECTURE = "all";
    protected static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    /**
     * The Maven project
     */
    @Parameter(required = true, readonly = true, defaultValue = "${project}")
    private MavenProject project;

    /**
     * Name of the generated DEB.
     *
     * @since 1.0.1
     */
    @Parameter(defaultValue = "${project.build.finalName}")
    private String finalName;

    /**
     * The application's Package name
     *
     * @since 1.1.0
     */
    @Parameter(defaultValue = "${project.artifactId}")
    private String packageName;

    /**
     * The application's version, for SNAPSHOT versions, the SNAPSHOT part is replaced with a sortable timestamp to
     * avoid unexpected upgrade behaviour
     *
     * @since 1.1.0
     */
    @Parameter(defaultValue = "${project.version}")
    private String version;

    /**
     * The application's Architecture
     *
     * @since 1.1.0
     */
    @Parameter(defaultValue = DEFAULT_ARCHITECTURE)
    private String architecture;

    /**
     * The application's homepage
     *
     * @since 1.1.0
     */
    @Parameter(defaultValue = "${project.url}")
    private String homepage;

    /**
     * The application's section
     *
     * @since 1.1.0
     */
    @Parameter
    private String section;

    /**
     * The application's priority
     *
     * @since 1.1.0
     */
    @Parameter
    private String priority;

    /**
     * The application's dependencies
     *
     * @since 1.1.0
     */
    @Parameter
    private String depends;

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
     *     &lt;expression>/etc/hiapp/*</expression>
     *     &lt;username>hiuser</username>
     *     &lt;groupname>wheel</groupname>
     *     &lt;mode>0700</mode>
     *     &lt;config>true</config>
     *   &lt;/folder>
     * &lt;/folders>
     * </pre>
     *
     * username is optional, default value = root<br>
     * groupname is optional, default value = root<br>
     * mode is optional, default value = 0755<br>
     * config marks the matching files as conffile, it is optional, default false<br>
     * one of username, groupname or mode should be specified or config should be true
     *
     * @since 1.1.0
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

    /**
     * Directory containing the generated DEB.
     *
     * @since 1.1.0
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;

    /**
     * The directory where the resources are to be found
     *
     * @since 1.1.0
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File resourcesDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ArchiveEntryCollector dataFilesCollector = new ArchiveEntryCollector();
        List<File> controlFiles = new ArrayList<File>();
        findFiles(getPackageBaseDir(), dataFilesCollector, controlFiles);
        if (dataFilesCollector.isEmpty()) {
            throw new MojoFailureException("Useless build, nothing to package");
        }
        File packageFile = getPackageFile();
        DebianPackage debianPackage = new DebianPackage(controlFiles, dataFilesCollector.getEntries(), getLog());
        debianPackage.write(packageFile);
        this.project.getArtifact().setFile(packageFile);
    }

    private File getPackageBaseDir() throws MojoExecutionException {
        File packageBasedir = new File(this.resourcesDirectory, DEBIAN_RESOURCES_DIRNAME);
        if (!packageBasedir.isDirectory()) {
            if (packageBasedir.exists()) {
                throw new MojoExecutionException(packageBasedir.getAbsolutePath() + " exists but is no directory");
            }
            if (!packageBasedir.mkdirs()) {
                throw new MojoExecutionException("Failed to create " + packageBasedir.getAbsolutePath());
            }
        }
        return packageBasedir;
    }

    protected File getPackageFile() throws MojoExecutionException {
        File packageFile = new File(getValidOutputDir(), this.finalName + DEBIAN_FILE_EXTENSION);
        getLog().info("Writing Debian package file to: " + packageFile.getAbsolutePath());
        return packageFile;
    }

    protected String getVersion() {
        if (this.version.endsWith(SNAPSHOT_SUFFIX)) {
            SimpleDateFormat format = new SimpleDateFormat(
                    this.version.substring(0, this.version.length() - SNAPSHOT_SUFFIX.length()) + "-yyyyMMddHHmmss");
            return format.format(new Date());
        }
        return this.version;
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
            StringBuilder maintainerFromModel = new StringBuilder();
            if (StringUtils.isNotBlank(dev.getName())) {
                maintainerFromModel.append(dev.getName().trim());
            }
            if (StringUtils.isNotBlank(dev.getEmail())) {
                if (maintainerFromModel.length() > 0) {
                    maintainerFromModel.append(" ");
                }
                maintainerFromModel.append("<").append(dev.getEmail()).append(">");
            }
            if (maintainerFromModel.length() > 0) {
                return maintainerFromModel.toString();
            }
        }
        return System.getProperty("user.name");
    }

    private void findFiles(File packageBasedir, ArchiveEntryCollector dataFilesCollector, Collection<File> controlFiles)
            throws MojoExecutionException, MojoFailureException {
        Set<File> conffiles = new HashSet<File>();
        ControlStatus controlStatus = new ControlStatus();
        handleFiles(packageBasedir, dataFilesCollector, controlFiles, controlStatus);
        if (this.artifacts != null && this.artifacts.length > 0) {
            handleArtifacts(dataFilesCollector);
        }
        if (this.folders != null && this.folders.length > 0) {
            handleFolders(dataFilesCollector);
        }
        if (this.attributes != null && this.attributes.length > 0) {
            handleAttributes(dataFilesCollector, conffiles);
        }
        if (!controlStatus.haveControl()) {
            controlFiles.add(generateControlFile(dataFilesCollector.getInstalledSize(), packageBasedir));
        }
        if (!controlStatus.haveConnffiles() && !conffiles.isEmpty()) {
            controlFiles.add(generateConnffilesFile(conffiles, packageBasedir));
        }
    }

    private void handleFiles(File packageBasedir, ArchiveEntryCollector dataFilesCollector,
            Collection<File> controlFiles, ControlStatus controlStatus) {
        for (File file : packageBasedir.listFiles()) {
            if (CONFFILES_DIRNAME.equals(file.getName())) {
                // add control files
                addControlFiles(controlFiles, controlStatus, file);
            } else {
                addDataFile(dataFilesCollector, file, SLASH);
            }
        }
    }

    private void handleAttributes(ArchiveEntryCollector dataFilesCollector, Set<File> conffiles)
            throws MojoFailureException {
        for (AttributeSelector attributeSelector : this.attributes) {
            if (attributeSelector.isValid()) {
                conffiles.addAll(dataFilesCollector.applyAttributes(attributeSelector.getExpression(),
                        attributeSelector.getUsername(), attributeSelector.getGroupname(),
                        getMode(attributeSelector.getMode(), attributeSelector.getExpression()),
                        attributeSelector.isConfig()));
            } else {
                throw new MojoFailureException("Invalid attributes specification " + attributeSelector.toString());
            }
        }
    }

    private void handleFolders(ArchiveEntryCollector dataFilesCollector) throws MojoFailureException {
        for (FolderEntry folder : this.folders) {
            if (folder.isValid()) {
                DirectoryArchiveEntry parentFolder = new DirectoryArchiveEntry(folder.getPath(), folder.getUsername(),
                        folder.getGroupname(), getMode(folder.getMode(), folder.getPath()));
                dataFilesCollector.add(parentFolder);
            } else {
                throw new MojoFailureException("Invalid folder specification " + folder.toString());
            }
        }
    }

    private void handleArtifacts(ArchiveEntryCollector dataFilesCollector) throws MojoFailureException {
        for (ArtifactPackageEntry artifactEntry : this.artifacts) {
            if (StringUtils.isNotBlank(artifactEntry.getDestination())) {
                Artifact depArtifact = getDependentArtifact(artifactEntry);
                File destFile = null;
                if (artifactEntry.getDestination().endsWith(SLASH)) {
                    destFile = new File(cleanPath(artifactEntry.getDestination() + depArtifact.getArtifactId() + DOT
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

    private void addControlFiles(Collection<File> controlFiles, ControlStatus controlStatus, File file) {
        for (File controlFile : file.listFiles()) {
            getLog().debug("Adding control " + controlFile.getAbsolutePath());
            if (CONTROL.getFilename().equals(controlFile.getName())) {
                controlStatus.setHaveControl();
            } else if (CONFFILES.getFilename().equals(controlFile.getName())) {
                controlStatus.setHaveConnffiles();
            }
            controlFiles.add(controlFile);
        }
    }

    private File generateConnffilesFile(Set<File> conffiles, File packageBasedir) throws MojoExecutionException {
        File conffilesFile = new File(getValidConfigResourcesDir(packageBasedir), CONFFILES.getFilename());
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(conffilesFile);
            for (File conffile : conffiles) {
                String path = conffile.getAbsolutePath().trim();
                if (StringUtils.isNotBlank(path)) {
                    byte[] pathBytes = path.getBytes();
                    fos.write(pathBytes);
                    fos.write('\n');
                }
            }
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Unable to create conffiles file", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write to conffiles file", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    getLog().error(e);
                }
            }
        }
        return conffilesFile;
    }

    private Artifact getDependentArtifact(ArtifactPackageEntry artifactEntry) throws MojoFailureException {
        Artifact dependentArtifact = null;
        if (artifactEntry.isValid()) {
            if (this.project.getDependencyArtifacts() != null) {
                dependentArtifact = findDependentArtifact(artifactEntry);
            }
        } else {
            throw new MojoFailureException("Invalid artifact specification");
        }
        if (dependentArtifact != null) {
            return dependentArtifact;
        }
        throw new MojoFailureException(String.format("Artifact %s not found", artifactEntry.toString()));
    }

    private Artifact findDependentArtifact(ArtifactPackageEntry artifactEntry) {
        for (Object depObj : this.project.getDependencyArtifacts()) {
            Artifact depArtifact = (Artifact) depObj;
            if (artifactEntry.matches(depArtifact)) {
                return depArtifact;
            }
        }
        return null;
    }

    private String cleanPath(String path) {
        String newPath = path.trim();
        if (newPath.endsWith(SLASH)) {
            newPath = path.substring(0, path.length() - 1);
        }
        if (newPath.startsWith(SLASH)) {
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

    private File generateControlFile(long installedSize, File packageBasedir) throws MojoExecutionException {
        DebianControl control = new DebianControl();
        control.setPackageName(this.packageName);
        control.setVersion(getVersion());
        control.setArchitecture(this.architecture);
        control.setMaintainer(getMaintainer());
        control.setDescriptionSynopsis(this.descriptionSynopsis);
        control.setDescription(this.description);
        control.setInstalledSize(getSizeKB(installedSize));
        control.setDepends(this.depends);
        control.setHomepage(this.homepage);
        control.setSection(this.section);
        control.setPriority(this.priority);
        File controlFile = new File(getValidConfigResourcesDir(packageBasedir), CONTROL.getFilename());
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

    protected long getSizeKB(long sizeByte) {
        return sizeByte / 1024 + (((sizeByte % 1024) > 0) ? 1 : 0);
    }

    private File getValidConfigResourcesDir(File packageBasedir) throws MojoExecutionException {
        File debianConffilesDir = new File(packageBasedir, CONFFILES_DIRNAME);
        if (!debianConffilesDir.isDirectory() && !debianConffilesDir.mkdirs()) {
            throw new MojoExecutionException(
                    "Unable to create debian resources directory: " + debianConffilesDir.getAbsolutePath());
        }
        return debianConffilesDir;
    }

    private File getValidOutputDir() throws MojoExecutionException {
        if (!this.outputDirectory.isDirectory() && !this.outputDirectory.mkdirs()) {
            throw new MojoExecutionException(
                    "Unable to create output directory: " + this.outputDirectory.getAbsolutePath());
        }
        return this.outputDirectory;
    }

    private void addDataFile(ArchiveEntryCollector dataFilesCollector, File datafile, String prefix) {
        getLog().debug("Adding data " + datafile.getAbsolutePath());
        String name = prefix + datafile.getName();
        if (datafile.isDirectory()) {
            DirectoryArchiveEntry dirEntry = new DirectoryArchiveEntry(name, this.defaultUsername,
                    this.defaultGroupname, getDefaultDirectoryMode());
            dataFilesCollector.add(dirEntry);
            for (File nestedDataFile : datafile.listFiles()) {
                addDataFile(dataFilesCollector, nestedDataFile, name + SLASH);
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

    protected void setFinalName(String finalName) {
        this.finalName = finalName;
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

    protected void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    protected void setSection(String section) {
        this.section = section;
    }

    protected void setPriority(String priority) {
        this.priority = priority;
    }

    protected void setDepends(String depends) {
        this.depends = depends;
    }

    protected void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    protected void setVersion(String version) {
        this.version = version;
    }

    protected void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setResourcesDirectory(File resourcesDirectory) {
        this.resourcesDirectory = resourcesDirectory;
    }

    private static class ControlStatus {
        boolean haveControl = false;
        boolean haveConnffiles = false;

        public boolean haveControl() {
            return this.haveControl;
        }

        public void setHaveControl() {
            this.haveControl = true;
        }

        public boolean haveConnffiles() {
            return this.haveConnffiles;
        }

        public void setHaveConnffiles() {
            this.haveConnffiles = true;
        }
    }
}
