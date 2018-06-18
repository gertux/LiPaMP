package be.hobbiton.maven.lipamp.deb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

public class DebianControl {
    public static final long INVALID_SIZE = -1L;
    private static final String FOLDED_FORMAT = " %s\n";
    private static final String SIMPLE_FIELD_FORMAT = "%s: %s\n";
    private static final String DECIMAL_FIELD_FORMAT = "%s: %d\n";
    private Log logger;
    private String packageName;
    private String section;
    private String priority;
    private String maintainer;
    private long installedSize = INVALID_SIZE;
    private String version;
    private String architecture;
    private String descriptionSynopsis;
    private String description;
    private String depends;
    private String homepage;

    public DebianControl() {
    }

    public DebianControl(File file, Log logger) throws FileNotFoundException {
        this(new FileInputStream(file), logger);
    }

    public DebianControl(InputStream input, Log logger) {
        this.logger = logger;
        parseControlFile(input);
    }

    public void write(OutputStream output) {
        if (!isValid()) {
            throw new DebianArchiveException("Control file is invalid");
        }
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        try {
            writeTextField(writer, DebianControlField.PACKAGE, this.packageName);
            writeTextField(writer, DebianControlField.VERSION, this.version);
            writeTextField(writer, DebianControlField.ARCHITECTURE, this.architecture);
            writeTextField(writer, DebianControlField.MAINTAINER, this.maintainer);
            writeTextField(writer, DebianControlField.DESCRIPTION, this.descriptionSynopsis);
            writer.write(String.format(FOLDED_FORMAT, this.description));
            writeNotEmptyTextField(writer, DebianControlField.SECTION, this.section);
            writeNotEmptyTextField(writer, DebianControlField.PRIORITY, this.priority);
            if (this.installedSize > INVALID_SIZE) {
                writer.write(String.format(DECIMAL_FIELD_FORMAT, DebianControlField.INSTALLED_SIZE.getFieldname(),
                        this.installedSize));
            }
            writeNotEmptyTextField(writer, DebianControlField.DEPENDS, this.depends);
            writeNotEmptyTextField(writer, DebianControlField.HOMEPAGE, this.homepage);
        } catch (IOException e) {
            throw new DebianArchiveException("Could not write control file", e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                this.logger.debug(e);
            }
        }
    }

    private void writeNotEmptyTextField(Writer writer, DebianControlField field, String value) throws IOException {
        if (StringUtils.isNotBlank(value)) {
            writeTextField(writer, field, value);
        }
    }

    private void writeTextField(Writer writer, DebianControlField field, String value) throws IOException {
        writer.write(String.format(SIMPLE_FIELD_FORMAT, field.getFieldname(), value));
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.packageName) && StringUtils.isNotBlank(this.version)
                && StringUtils.isNotBlank(this.architecture) && StringUtils.isNotBlank(this.maintainer)
                && StringUtils.isNotBlank(this.description) && StringUtils.isNotBlank(this.descriptionSynopsis);
    }

    private final void parseControlFile(InputStream input) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        String line = null;
        String fieldName = null;
        List<String> values = null;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.charAt(0) == ' ' || line.charAt(0) == '\t') {
                    addContinuationLine(line, values);
                } else {
                    String[] parts = line.split(":", 2);
                    addFieldValue(parts, fieldName, values);
                    fieldName = parts[0];
                    values = new ArrayList<>();
                    values.add(parts[1].trim());
                }
            }
            if (fieldName != null) {
                saveControlField(fieldName, values);
            }
        } catch (IOException e) {
            throw new DebianArchiveException("Unable to read Control File", e);
        }
    }

    private void addFieldValue(String[] parts, String fieldName, List<String> values) {
        if (parts.length != 2 || parts[0].length() < 1) {
            throw new DebianArchiveException("Unable to read Control File, unexpected line: " + parts);
        }
        if (fieldName != null) {
            saveControlField(fieldName, values);
        }
    }

    private void addContinuationLine(String line, List<String> values) {
        if (values != null) {
            values.add(line.trim());
        } else {
            throw new DebianArchiveException(
                    "Unable to read Control File continuation, unexpected line: " + line);
        }
    }

    private String readDescription(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values.subList(1, values.size())) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(value);
        }
        return sb.toString();
    }

    private final void saveControlField(String fieldName, List<String> values) {
        DebianControlField field = null;
        try {
            field = DebianControlField.fromFieldname(fieldName);
        } catch (IllegalArgumentException e) {
            this.logger.warn("Unknown control field: " + fieldName);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug(e);
            }
            return;
        }
        if (DebianControlField.DESCRIPTION.equals(field)) {
            setDescriptionSynopsis(values.get(0));
            if (values.size() > 1) {
                setDescription(readDescription(values));
            }
        } else {
            saveSimpleField(values, field);
        }
    }

    private void saveSimpleField(List<String> values, DebianControlField field) {
        switch (field) {
        case PACKAGE:
            setPackageName(values.get(0));
            break;

        case SECTION:
            setSection(values.get(0));
            break;

        case PRIORITY:
            setPriority(values.get(0));
            break;

        case MAINTAINER:
            setMaintainer(values.get(0));
            break;

        case INSTALLED_SIZE:
            setInstalledSize(getLongValue(values.get(0)));
            break;

        case VERSION:
            setVersion(values.get(0));
            break;

        case ARCHITECTURE:
            setArchitecture(values.get(0));
            break;

        case DEPENDS:
            setDepends(values.get(0));
            break;

        case HOMEPAGE:
            setHomepage(values.get(0));
            break;

        default:
            break;
        }
    }

    public long getLongValue(String strValue) {
        try {
            return Long.parseLong(strValue);
        } catch (NumberFormatException e) {
            return INVALID_SIZE;
        }
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSection() {
        return this.section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getPriority() {
        return this.priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getMaintainer() {
        return this.maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public long getInstalledSize() {
        return this.installedSize;
    }

    public void setInstalledSize(long installedSize) {
        this.installedSize = installedSize;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getArchitecture() {
        return this.architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getDescriptionSynopsis() {
        return this.descriptionSynopsis;
    }

    public void setDescriptionSynopsis(String descriptionSynopsis) {
        this.descriptionSynopsis = descriptionSynopsis;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepends() {
        return this.depends;
    }

    public void setDepends(String depends) {
        this.depends = depends;
    }

    public String getHomepage() {
        return this.homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public enum DebianControlField {
        DEPENDS("Depends"), ARCHITECTURE("Architecture"), VERSION("Version"), INSTALLED_SIZE(
                "Installed-Size"), MAINTAINER("Maintainer"), PRIORITY("Priority"), SECTION("Section"), PACKAGE(
                        "Package"), DESCRIPTION("Description"), HOMEPAGE("Homepage");
        private final String fieldname;

        DebianControlField(String n) {
            this.fieldname = n;
        }

        public final String getFieldname() {
            return this.fieldname;
        }

        public static DebianControlField fromFieldname(String n) {
            for (DebianControlField f : DebianControlField.values()) {
                if (f.fieldname.equals(n)) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown Field name: " + n);
        }
    }
}
