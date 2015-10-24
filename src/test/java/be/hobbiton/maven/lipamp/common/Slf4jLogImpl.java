package be.hobbiton.maven.lipamp.common;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLogImpl implements Log {
    private static final Logger LOGGER = LoggerFactory.getLogger(Slf4jLogImpl.class);

    @Override
    public boolean isDebugEnabled() {
        return LOGGER.isDebugEnabled();
    }

    @Override
    public void debug(CharSequence content) {
        LOGGER.debug("{}", content);
    }

    @Override
    public void debug(CharSequence content, Throwable error) {
        LOGGER.debug("{}", content, error);
    }

    @Override
    public void debug(Throwable error) {
        LOGGER.debug("", error);
    }
    @Override
    public boolean isInfoEnabled() {
        return LOGGER.isInfoEnabled();
    }

    @Override
    public void info(CharSequence content) {
        LOGGER.info("{}", content);
    }

    @Override
    public void info(CharSequence content, Throwable error) {
        LOGGER.info("{}", content, error);
    }

    @Override
    public void info(Throwable error) {
        LOGGER.info("", error);
    }

    @Override
    public boolean isWarnEnabled() {
        return LOGGER.isWarnEnabled();
    }

    @Override
    public void warn(CharSequence content) {
        LOGGER.warn("{}", content);
    }

    @Override
    public void warn(CharSequence content, Throwable error) {
        LOGGER.warn("{}", content, error);
    }

    @Override
    public void warn(Throwable error) {
        LOGGER.warn("", error);
    }

    @Override
    public boolean isErrorEnabled() {
        return LOGGER.isErrorEnabled();
    }

    @Override
    public void error(CharSequence content) {
        LOGGER.error("{}", content);
    }

    @Override
    public void error(CharSequence content, Throwable error) {
        LOGGER.error("{}", content, error);
    }

    @Override
    public void error(Throwable error) {
        LOGGER.error("", error);
    }
}
