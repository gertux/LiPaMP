package be.hobbiton.maven.lipamp.common;

/**
 * Internal Runtime version of {@link org.apache.maven.plugin.MojoExecutionException}
 *
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 */
public class LinuxPackagingException extends RuntimeException {
    public LinuxPackagingException(String message) {
        super(message);
    }

    public LinuxPackagingException(String message, Throwable cause) {
        super(message, cause);
    }

}
