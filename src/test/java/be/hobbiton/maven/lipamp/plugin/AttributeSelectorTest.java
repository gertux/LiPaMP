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
        assertTrue(new AttributeSelector(EXPRESSION, USERNAME, GROUPNAME, MODE).isValid());
        assertTrue(new AttributeSelector(EXPRESSION, USERNAME, GROUPNAME, null).isValid());
        assertTrue(new AttributeSelector(EXPRESSION, USERNAME, null, null).isValid());
        assertTrue(new AttributeSelector(EXPRESSION, null, GROUPNAME, null).isValid());
        assertTrue(new AttributeSelector(EXPRESSION, null, GROUPNAME, MODE).isValid());
        assertTrue(new AttributeSelector(EXPRESSION, null, null, MODE).isValid());
        assertTrue(new AttributeSelector(EXPRESSION, USERNAME, null, MODE).isValid());
    }

    @Test
    public void testIsInvalid() {
        assertFalse(new AttributeSelector(null, null, null, null).isValid());
        assertFalse(new AttributeSelector(EXPRESSION, null, null, null).isValid());
        assertFalse(new AttributeSelector(null, USERNAME, null, null).isValid());
        assertFalse(new AttributeSelector(null, null, GROUPNAME, null).isValid());
        assertFalse(new AttributeSelector(null, null, null, MODE).isValid());
        assertFalse(new AttributeSelector(null, null, GROUPNAME, MODE).isValid());
        assertFalse(new AttributeSelector(null, USERNAME, null, MODE).isValid());
        assertFalse(new AttributeSelector(null, USERNAME, GROUPNAME, null).isValid());
        assertFalse(new AttributeSelector(null, USERNAME, GROUPNAME, MODE).isValid());
    }
}
