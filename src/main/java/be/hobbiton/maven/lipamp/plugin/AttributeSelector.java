package be.hobbiton.maven.lipamp.plugin;

import org.codehaus.plexus.util.StringUtils;

public class AttributeSelector extends Attributable {
    private String expression;
    private boolean config = false;

    public AttributeSelector() {
    }

    public AttributeSelector(String expression, String username, String groupname, String mode, boolean config) {
        super();
        this.expression = expression;
        this.config = config;
        setUsername(username);
        setGroupname(groupname);
        setMode(mode);
    }

    public boolean isValid() {
        return (StringUtils.isNotBlank(this.expression)) && (this.config || StringUtils.isNotBlank(getUsername())
                || StringUtils.isNotBlank(getGroupname()) || StringUtils.isNotBlank(getMode()));
    }

    public String getExpression() {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public boolean isConfig() {
        return this.config;
    }

    public void setConfig(boolean config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return String.format("path expression=%s u=%s g=%s m=%s %s", this.expression, getUsername(), getGroupname(),
                getMode(), (this.config) ? "config" : "");
    }
}
