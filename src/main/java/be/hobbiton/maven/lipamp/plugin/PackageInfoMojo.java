/* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
   distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

   Copyright 2019 Gert Dewit <gert@hobbiton.be>
*/
package be.hobbiton.maven.lipamp.plugin;

import java.io.File;

import be.hobbiton.maven.lipamp.common.LinuxPackagingException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import be.hobbiton.maven.lipamp.deb.DebInfo;

/**
 * Print information about a package
 *
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 * @since 1.2.0
 */
@Mojo(name = "info", requiresProject = false)
public class PackageInfoMojo extends AbstractMojo {
    /**
     * The package file
     */
    @Parameter(property = "file", required = true)
    private File file;

    @Override
    public void execute() throws MojoExecutionException {
        if (!this.file.exists()) {
            throw new MojoExecutionException(this.file.getPath() + " does not exist");
        }
        try {
            DebInfo info = new DebInfo(this.file, getLog());
            getLog().info(info.toString());
        } catch (LinuxPackagingException e) {
            throw new MojoExecutionException("Failed to process " + this.file.getPath(), e);
        }
    }

    protected void setFile(File file) {
        this.file = file;
    }
}
