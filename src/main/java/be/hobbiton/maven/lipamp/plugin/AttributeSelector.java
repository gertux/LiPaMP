package be.hobbiton.maven.lipamp.plugin;

import org.codehaus.plexus.util.StringUtils;

public class AttributeSelector {
    private String expression;
    private String username;
    private String groupname;
    private String mode;
    private boolean config = false;

    public AttributeSelector() {
    }

    public AttributeSelector(String expression, String username, String groupname, String mode, boolean config) {
        super();
        this.expression = expression;
        this.username = username;
        this.groupname = groupname;
        this.mode = mode;
        this.config = config;
    }

    public boolean isValid() {
        return ((StringUtils.isNotBlank(this.expression)) && (this.config || StringUtils.isNotBlank(this.username)
                || StringUtils.isNotBlank(this.groupname) || StringUtils.isNotBlank(this.mode)));
    }

    public String getExpression() {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGroupname() {
        return this.groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getMode() {
        return this.mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isConfig() {
        return this.config;
    }

    public void setConfig(boolean config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return String.format("path expression=%s u=%s g=%s m=%s", this.expression, this.username, this.groupname,
                this.mode);
    }
}
