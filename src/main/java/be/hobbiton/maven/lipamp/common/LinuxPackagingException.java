/* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
   distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

   Copyright 2019 Gert Dewit <gert@hobbiton.be>
*/
package be.hobbiton.maven.lipamp.common;

/**
 * Internal Runtime version of {@link org.apache.maven.plugin.MojoExecutionException}
 */
public class LinuxPackagingException extends RuntimeException {
    public LinuxPackagingException(String message) {
        super(message);
    }

    public LinuxPackagingException(String message, Throwable cause) {
        super(message, cause);
    }

}
