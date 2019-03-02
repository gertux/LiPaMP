/* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
   distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

   Copyright 2019 Gert Dewit <gert@hobbiton.be>
*/
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
