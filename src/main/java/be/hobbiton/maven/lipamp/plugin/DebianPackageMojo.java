package be.hobbiton.maven.lipamp.plugin;

import be.hobbiton.maven.lipamp.common.*;
import be.hobbiton.maven.lipamp.deb.DebianControl;
import be.hobbiton.maven.lipamp.deb.DebianPackage;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Developer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.SelectorUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static be.hobbiton.maven.lipamp.common.ArchiveEntry.*;
import static be.hobbiton.maven.lipamp.common.Constants.*;
import static be.hobbiton.maven.lipamp.deb.DebInfo.DebianInfoFile.CONFFILES;
import static be.hobbiton.maven.lipamp.deb.DebInfo.DebianInfoFile.CONTROL;

/**
 * Create a Debian package.
 * <p>
 * Binds to the package lifecycle phase for artifacts of type deb
 *
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 * @since 1.0.0
 */
@Mojo(name = "makedeb", requiresProject = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class DebianPackageMojo extends AbstractMojo {
    public static final String DEBIAN_PACKAGING_TYPE = "deb";
    public static final String DEBIAN_RESOURCES_DIR_NAME = "deb";
    public static final String DEBIAN_FILE_EXTENSION = ".deb";
    public static final String DEBIAN_CONTROL_FILES_DIR_NAME = "DEBIAN";
    public static final Path DEBIAN_CONTROL_FILES_DIR_PATH = Paths.get(DEBIAN_CONTROL_FILES_DIR_NAME);
    /**
     * As the targets for this mojo are primary Java apps, the package is by default architecture independent
     */
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
     * <p>
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
     * <p>
     * username is optional, default value = root<br>
     * groupname is optional, default value = root<br>
     * mode is optional, default value = 0755
     *
     * @since 1.0.0
     */
    @Parameter
    private FolderEntry[] folders;

    /**
     * The Symbolic links that should be created and packaged.
     *
     * <pre>
     * &lt;links>
     *   &lt;link>
     *     &lt;path>/var/opt/data</path>
     *     &lt;path>../../data</path>
     *     &lt;username>hiuser</username>
     *     &lt;groupname>wheel</groupname>
     *     &lt;mode>0700</mode>
     *   &lt;/link>
     * &lt;/links>
     * </pre>
     * <p>
     * username is optional, default value = root<br>
     * groupname is optional, default value = root<br>
     * mode is optional, default value = 0644<br>
     *
     * @since 1.2.0
     */
    @Parameter
    private LinkEntry[] links;

    /**
     * Add the project's dependencies
     *
     * <pre>
     * &lt;dependencies>
     *     &lt;destination>/opt/lib/</destination>
     *     &lt;username>hiuser</username>
     *     &lt;groupname>wheel</groupname>
     *     &lt;mode>0700</mode>
     *     &lt;type>maven</type>
     * &lt;/dependencies>
     * </pre>
     * <p>
     * username is optional, default value = root<br>
     * groupname is optional, default value = root<br>
     * mode is optional, default value = 0755<br>
     * type is optional, default value = maven<br>
     * type can be:
     * <ul>
     * <li>maven: maven repo style directories</li>
     * <li>flat: all files in the destination directory</li>
     * </ul>
     *
     * @since 1.2.0
     */
    @Parameter
    private Dependencies dependencies;

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
     * <p>
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

    private Path packageBasePath;
    private Path debianControlFilesBasePath;
    private List<ConfigFileSelector> configFileSelectors;
    private List<AttributeSelector> attributeSelectors;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.configure();
        TreeSet<ArchiveEntry> dataArchiveEntries = new TreeSet<>(Comparator.comparing(ArchiveEntry::getName));
        dataArchiveEntries.addAll(getLinkEntries());
        dataArchiveEntries.addAll(getFolderEntries());
        Set<Artifact> configuredArtifacts = getArtifacts();
        dataArchiveEntries.addAll(getArtifactEntries());
        dataArchiveEntries.addAll(getDependencies(configuredArtifacts));
        dataArchiveEntries.addAll(collectDataArchiveEntries(this.packageBasePath));
        if (dataArchiveEntries.size() <= 1) {
            throw new MojoFailureException("Useless build, nothing to package");
        }
        generateConffilesFile(this.debianControlFilesBasePath.resolve(CONFFILES.getFilename()), dataArchiveEntries);
        generateControlFile(getIstalledSize(dataArchiveEntries), this.debianControlFilesBasePath);
        List<Path> controlPaths = collectControlPaths(this.debianControlFilesBasePath);
        File packageFile = getPackageFile();
        DebianPackage debianPackage = new DebianPackage(getLog(), controlPaths, dataArchiveEntries);
        debianPackage.write(packageFile);
        this.project.getArtifact().setFile(packageFile);
    }

    private void generateConffilesFile(Path conffilesPath, TreeSet<ArchiveEntry> dataArchiveEntries) throws MojoExecutionException {
        File conffilesFile = conffilesPath.toFile();
        if (conffilesFile.isFile()) {
            return;
        }
        List<ArchiveEntry> confFiles = dataArchiveEntries.stream().filter(this::isConfigFile).collect(Collectors.toList());
        if (confFiles.isEmpty()) {
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(conffilesFile)) {
            for (ArchiveEntry archiveEntry : confFiles) {
                fos.write(archiveEntry.getAbsoluteName().getBytes());
                fos.write('\n');
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write to conffiles file", e);
        }
    }

    private boolean isConfigFile(ArchiveEntry archiveEntry) {
        if (!ArchiveEntry.ArchiveEntryType.F.equals(archiveEntry.getType())) {
            return false;
        }
        for (ConfigFileSelector selector : this.configFileSelectors) {
            if (SelectorUtils.matchPath(selector.getExpression(), archiveEntry.getAbsoluteName())) {
                return true;
            }
        }
        return false;
    }

    private void configure() throws MojoFailureException, MojoExecutionException {
        this.defaultUsername = stringValueOrDefault(this.defaultUsername, DEFAULT_USERNAME);
        this.defaultGroupname = stringValueOrDefault(this.defaultGroupname, DEFAULT_GROUPNAME);
        this.defaultDirectoryModeValue = modeValueOrDefault(ArchiveEntry.fromModeString(this.defaultDirectoryMode), DEFAULT_DIRMODE_VALUE);
        this.defaultFileModeValue = modeValueOrDefault(ArchiveEntry.fromModeString(this.defaultFileMode), DEFAULT_FILEMODE_VALUE);
        this.packageBasePath = this.resourcesDirectory.toPath().resolve(DEBIAN_RESOURCES_DIR_NAME);
        this.debianControlFilesBasePath = packageBasePath.resolve(DEBIAN_CONTROL_FILES_DIR_PATH);
        configureAttributeSelectors();
        configureDependencies();
        createFolderIfMissing(this.debianControlFilesBasePath);
    }

    private void configureDependencies() {
        if (this.dependencies != null) {
            this.dependencies.setUsername(stringValueOrDefault(this.dependencies.getUsername(), this.defaultUsername));
            this.dependencies.setGroupname(stringValueOrDefault(this.dependencies.getGroupname(), this.defaultGroupname));
            this.dependencies.setModeValue(modeValueOrDefault(this.dependencies.getModeValue(), this.defaultFileModeValue));
            this.dependencies.setDirModeValue(modeValueOrDefault(this.dependencies.getDirModeValue(), this.defaultDirectoryModeValue));
        }
    }

    private List<ArchiveEntry> getLinkEntries() {
        if (this.links != null) {
            return Arrays.stream(this.links).flatMap(this::toArchiveEntries).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Stream<ArchiveEntry> toArchiveEntries(LinkEntry linkEntry) {
        List<ArchiveEntry> archiveEntries = new ArrayList<>();
        if (linkEntry.isValid()) {
            Path destPath = Paths.get(linkEntry.getPath());
            if (destPath.startsWith(ROOT_PATH)) {
                destPath = ROOT_PATH.relativize(destPath);
            }
            archiveEntries.addAll(getParentEntries(destPath.getParent()));
            archiveEntries.add(new SymbolicLinkArchiveEntry(String.valueOf(CURRENT_PATH.resolve(destPath)), linkEntry.getTarget(), stringValueOrDefault
                    (linkEntry.getUsername(), this.defaultUsername), stringValueOrDefault(linkEntry.getGroupname(), this.defaultGroupname),
                    modeValueOrDefault(linkEntry.getModeValue(), this.defaultDirectoryModeValue)));
        }
        return archiveEntries.stream();
    }

    private List<ArchiveEntry> getFolderEntries() {
        if (this.folders != null) {
            return Arrays.stream(this.folders).flatMap(this::toArchiveEntries).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Stream<ArchiveEntry> toArchiveEntries(FolderEntry folderEntry) {
        List<ArchiveEntry> archiveEntries = new ArrayList<>();
        if (folderEntry.isValid()) {
            Path destPath = Paths.get(folderEntry.getPath());
            if (destPath.startsWith(ROOT_PATH)) {
                destPath = ROOT_PATH.relativize(destPath);
            }
            archiveEntries.addAll(getParentEntries(destPath.getParent()));
            archiveEntries.add(new DirectoryArchiveEntry(String.valueOf(CURRENT_PATH.resolve(destPath)), stringValueOrDefault(folderEntry.getUsername(), this
                    .defaultUsername), stringValueOrDefault(folderEntry.getGroupname(), this.defaultGroupname), modeValueOrDefault(folderEntry.getModeValue()
                    , this.defaultDirectoryModeValue)));
        }
        return archiveEntries.stream();
    }

    private List<ArchiveEntry> getParentEntries(Path parent) {
        List<ArchiveEntry> archiveEntries = new ArrayList<>();
        if (parent != null) {
            Path dir = Paths.get("");
            for (Path pathElem : parent) {
                dir = dir.resolve(pathElem);
                ArchiveEntry archiveEntry = toArchiveEntry(dir, null, null, ArchiveEntryType.D);
                archiveEntries.add(archiveEntry);
            }
        }
        return archiveEntries;
    }

    private List<ArchiveEntry> getDependencies(Set<Artifact> configuredArtifacts) {
        if (this.dependencies != null) {
            ArtifactStore artifactStore = this.dependencies.getArtifactStore(this.defaultUsername, this.defaultGroupname, this.defaultFileModeValue, this
                    .defaultDirectoryModeValue);
            Set<Artifact> allDependencies = this.project.getArtifacts();
            return allDependencies.stream().
                    filter(a -> !(configuredArtifacts.contains(a))).
                    flatMap(artifactStore::toArchiveEntries).
                    collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Set<Artifact> getArtifacts() {
        if (this.artifacts != null) {
            return Arrays.stream(this.artifacts).
                    map(this::getDependentArtifact).
                    collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    protected void setArtifacts(ArtifactPackageEntry[] artifacts) {
        this.artifacts = artifacts;
    }

    private List<ArchiveEntry> getArtifactEntries() {
        if (this.artifacts != null) {
            return Arrays.stream(this.artifacts).
                    filter(ae -> StringUtils.isNotBlank(ae.getDestination())).
                    flatMap(this::toArchiveEntries).
                    collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Stream<ArchiveEntry> toArchiveEntries(ArtifactPackageEntry artifactEntry) {
        List<ArchiveEntry> archiveEntries = new ArrayList<>();
        Artifact depArtifact = getDependentArtifact(artifactEntry);
        Path destPath;
        if (artifactEntry.getDestination().endsWith(SLASH)) {
            destPath = Paths.get(artifactEntry.getDestination().concat(depArtifact.getArtifactId().concat(DOT).concat(depArtifact.getType())));
        } else {
            destPath = Paths.get(artifactEntry.getDestination());
        }
        if (destPath.startsWith(ROOT_PATH)) {
            destPath = ROOT_PATH.relativize(destPath);
        }
        archiveEntries.addAll(getParentEntries(destPath.getParent()));
        archiveEntries.add(new FileArchiveEntry(String.valueOf(CURRENT_PATH.resolve(destPath)), depArtifact.getFile(), stringValueOrDefault(artifactEntry
                .getUsername(), this.defaultUsername), stringValueOrDefault(artifactEntry.getGroupname(), this.defaultGroupname), modeValueOrDefault
                (artifactEntry.getModeValue(), this.defaultFileModeValue)));
        return archiveEntries.stream();
    }

    private long getIstalledSize(Collection<ArchiveEntry> dataArchiveEntries) {
        return dataArchiveEntries.stream().mapToLong(ArchiveEntry::getSize).sum();
    }

    private void configureAttributeSelectors() throws MojoFailureException {
        this.configFileSelectors = new ArrayList<>();
        this.attributeSelectors = new ArrayList<>();
        if (this.attributes != null) {
            for (AttributeSelector attributeSelector : this.attributes) {
                if (attributeSelector.isValid()) {
                    if (attributeSelector.isConfig()) {
                        this.configFileSelectors.add(ConfigFileSelector.fromAttributeSelector(attributeSelector));
                    }
                    this.attributeSelectors.add(attributeSelector);
                } else {
                    throw new MojoFailureException("Invalid attributes specification " + attributeSelector.toString());
                }
            }
        }
    }

    private List<Path> collectControlPaths(Path confFilesPath) throws MojoExecutionException {
        if (!confFilesPath.toFile().isDirectory()) {
            return Collections.emptyList();
        }
        try (Stream<Path> fileList = Files.list(confFilesPath)) {
            return fileList.filter(Files::isRegularFile).collect(Collectors.toList());
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot collect control files", e);
        }
    }

    private List<ArchiveEntry> collectDataArchiveEntries(Path dataFilesPath) throws MojoExecutionException {
        try (Stream<Path> fileList = Files.walk(dataFilesPath)) {
            return fileList.map(this::relativePath).filter(this::isDataPath).map(this::toArchiveEntry).collect(Collectors.toList());
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot collect data files", e);
        }
    }

    private Path relativePath(Path path) {
        return this.packageBasePath.relativize(path);
    }

    private boolean isDataPath(Path path) {
        return !path.startsWith(DEBIAN_CONTROL_FILES_DIR_PATH);
    }

    private ArchiveEntry toArchiveEntry(Path relpath) {
        ArchiveEntry archiveEntry;
        Path path = packageBasePath.resolve(relpath);
        if (Files.isSymbolicLink(path)) {
            archiveEntry = toArchiveEntry(relpath, null, getLinkTarget(path), ArchiveEntry.ArchiveEntryType.S);
        } else if (path.toFile().isDirectory()) {
            archiveEntry = toArchiveEntry(relpath, null, null, ArchiveEntry.ArchiveEntryType.D);
        } else {
            archiveEntry = toArchiveEntry(relpath, path, null, ArchiveEntry.ArchiveEntryType.F);
        }
        return archiveEntry;
    }

    private String getLinkTarget(Path path) {
        try {
            return String.valueOf(Files.readSymbolicLink(path));
        } catch (IOException e) {
            throw new LinuxPackagingMojoException("Cannot read link information for ".concat(String.valueOf(path)), e);
        }
    }

    private ArchiveEntry toArchiveEntry(Path relpath, Path contents, String link, ArchiveEntry.ArchiveEntryType type) {
        String userName = this.defaultUsername;
        String groupName = this.defaultGroupname;
        int fileMode = this.defaultFileModeValue;
        int dirMode = this.defaultDirectoryModeValue;
        for (AttributeSelector attributeSelector : this.attributeSelectors) {
            if (SelectorUtils.matchPath(attributeSelector.getExpression(), SLASH.concat(String.valueOf(relpath)))) {
                userName = stringValueOrDefault(attributeSelector.getUsername(), userName);
                groupName = stringValueOrDefault(attributeSelector.getGroupname(), groupName);
                fileMode = modeValueOrDefault(attributeSelector.getModeValue(), fileMode);
                dirMode = modeValueOrDefault(attributeSelector.getModeValue(), dirMode);
            }
        }
        switch (type) {
            case S:
                return new SymbolicLinkArchiveEntry(String.valueOf(CURRENT_PATH.resolve(relpath)), link, userName, groupName, dirMode);
            case D:
                return new DirectoryArchiveEntry(String.valueOf(CURRENT_PATH.resolve(relpath)), userName, groupName, dirMode);
            default:
                if (contents != null) {
                    File contentsFile = contents.toFile();
                    return new FileArchiveEntry(String.valueOf(CURRENT_PATH.resolve(relpath)), contentsFile, userName, groupName, fileMode);
                } else {
                    throw new LinuxPackagingMojoException("No contents for file entry ".concat(relpath.toString()));
                }
        }
    }

    protected File getPackageFile() throws MojoExecutionException {
        File packageFile = new File(getValidOutputDir(), this.finalName + DEBIAN_FILE_EXTENSION);
        getLog().info("Writing Debian package file to: " + packageFile.getAbsolutePath());
        return packageFile;
    }

    protected String getVersion() {
        if (this.version.endsWith(SNAPSHOT_SUFFIX)) {
            SimpleDateFormat format = new SimpleDateFormat(this.version.substring(0, this.version.length() - SNAPSHOT_SUFFIX.length()) + "-yyyyMMddHHmmss");
            return format.format(new Date());
        }
        return this.version;
    }

    protected void setVersion(String version) {
        this.version = version;
    }

    protected String getMaintainer() {
        if (StringUtils.isNotBlank(this.maintainer)) {
            return this.maintainer;
        }
        return getMaintainerFromModel();
    }

    protected void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
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

    private Artifact getDependentArtifact(ArtifactPackageEntry artifactEntry) {
        Artifact dependentArtifact = null;
        if (artifactEntry.isValid()) {
            if (this.project.getDependencyArtifacts() != null) {
                dependentArtifact = findDependentArtifact(artifactEntry);
            }
        } else {
            throw new LinuxPackagingMojoException("Invalid artifact specification");
        }
        if (dependentArtifact != null) {
            return dependentArtifact;
        }
        throw new LinuxPackagingMojoException(String.format("Artifact %s not found", artifactEntry.toString()));
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

    private boolean createFolderIfMissing(Path folderPath) throws MojoExecutionException {
        boolean created = false;
        if (!folderPath.toFile().isDirectory()) {
            try {
                Files.createDirectories(folderPath);
                created = true;
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot create folder ".concat(String.valueOf(folderPath)), e);
            }
        }
        return created;
    }

    private File generateControlFile(long installedSize, Path confFilesBasePath) throws MojoExecutionException {
        File controlFile = new File(confFilesBasePath.toFile(), CONTROL.getFilename());
        if (!controlFile.isFile()) {
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
            try (FileOutputStream fos = new FileOutputStream(controlFile)) {
                control.write(fos);
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to create control file", e);
            }
        }
        return controlFile;
    }

    protected long getSizeKB(long sizeByte) {
        return sizeByte / 1024 + (((sizeByte % 1024) > 0) ? 1 : 0);
    }

    private File getValidOutputDir() throws MojoExecutionException {
        if (!this.outputDirectory.isDirectory() && !this.outputDirectory.mkdirs()) {
            throw new MojoExecutionException("Unable to create output directory: " + this.outputDirectory.getAbsolutePath());
        }
        return this.outputDirectory;
    }

    protected void setDefaultDirectoryMode(String defaultDirectoryMode) {
        this.defaultDirectoryMode = defaultDirectoryMode;
    }

    protected void setDefaultFileMode(String defaultFileMode) {
        this.defaultFileMode = defaultFileMode;
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

    protected void setDescriptionSynopsis(String descriptionSynopsis) {
        this.descriptionSynopsis = descriptionSynopsis;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected void setFolders(FolderEntry[] folders) {
        this.folders = folders;
    }

    protected void setLinks(LinkEntry[] links) {
        this.links = links;
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

    protected void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setResourcesDirectory(File resourcesDirectory) {
        this.resourcesDirectory = resourcesDirectory;
    }

    public void setDependencies(Dependencies dependencies) {
        this.dependencies = dependencies;
    }
}
