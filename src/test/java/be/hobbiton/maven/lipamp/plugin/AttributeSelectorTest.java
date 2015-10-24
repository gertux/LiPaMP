package be.hobbiton.maven.lipamp.plugin;

import static org.junit.Assert.*;

import org.junit.Test;

public class AttributeSelectorTest {
    private static final String USERNAME = "username";
    private static final String GROUPNAME = "groupname";
    private static final String MODE = "0755";
    private static final String EXPRESSION = "/**";

    @Test
    public void testIsValid() {
        assertTrue(new AttributeSelector(EXPRESSION, USERNAME, GROUPNAME, MODE, false).isValid());
        assertTrue(new AttributeSelector(EXPRESSION, USERNAME, GROUPNAME, MODE, true).isValid());
        assertTrue(new AttributeSelector(EXPRESSION, USERNAME, GROUPNAME, null, false).isValid());
        assertTrue(new AttributeSelector(EXPRESSION, USERNAME, null, null, true).isValid());
        assertTrue(new AttributeSelector(EXPRESSION, null, GROUPNAME, null, false).isValid());
        assertTrue(new AttributeSelector(EXPRESSION, null, GROUPNAME, MODE, true).isValid());
        assertTrue(new AttributeSelector(EXPRESSION, null, null, MODE, false).isValid());
        assertTrue(new AttributeSelector(EXPRESSION, USERNAME, null, MODE, true).isValid());
        assertTrue(new AttributeSelector(EXPRESSION, null, null, null, true).isValid());
    }

    @Test
    public void testIsInvalid() {
        assertFalse(new AttributeSelector(null, null, null, null, true).isValid());
        assertFalse(new AttributeSelector(EXPRESSION, null, null, null, false).isValid());
        assertFalse(new AttributeSelector(null, USERNAME, null, null, true).isValid());
        assertFalse(new AttributeSelector(null, null, GROUPNAME, null, true).isValid());
        assertFalse(new AttributeSelector(null, null, null, MODE, true).isValid());
        assertFalse(new AttributeSelector(null, null, GROUPNAME, MODE, true).isValid());
        assertFalse(new AttributeSelector(null, USERNAME, null, MODE, true).isValid());
        assertFalse(new AttributeSelector(null, USERNAME, GROUPNAME, null, true).isValid());
        assertFalse(new AttributeSelector(null, USERNAME, GROUPNAME, MODE, true).isValid());
        assertFalse(new AttributeSelector(null, USERNAME, GROUPNAME, MODE, true).isValid());
    }

    @Test
    public void testMiscValid() {
        AttributeSelector entry = new AttributeSelector();
        assertFalse(entry.isValid());
        entry.setExpression(EXPRESSION);
        assertFalse(entry.isValid());
        entry.setConfig(true);
        assertTrue(entry.isValid());
        assertTrue(entry.toString().contains(EXPRESSION));
    }
}
