package be.hobbiton.maven.lipamp.plugin;

import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 */
public class LinkEntry extends Attributable{
    private String path;
    private String target;

    public LinkEntry() {
    }

    public LinkEntry(String path, String target, String username, String groupname, String mode) {
        this.path = path;
        this.target = target;
        setUsername(username);
        setGroupname(groupname);
        setMode(mode);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean isValid() {
        return (StringUtils.isNotBlank(this.path) && StringUtils.isNotBlank(this.target));
    }

    @Override
    public String toString() {
        return String.format("%s -> %s u=%s g=%s m=%s", this.path, this.target, getUsername(), getGroupname(), getMode());
    }
}
