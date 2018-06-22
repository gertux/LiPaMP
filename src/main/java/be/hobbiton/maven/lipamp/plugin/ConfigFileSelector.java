package be.hobbiton.maven.lipamp.plugin;

/**
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 */
public class ConfigFileSelector {
    private String expression;

    public ConfigFileSelector(String expression) {
        this.expression = expression;
    }

    public static ConfigFileSelector fromAttributeSelector(AttributeSelector attributeSelector) {
        return new ConfigFileSelector(attributeSelector.getExpression());
    }

    public String getExpression() {
        return expression;
    }
}
