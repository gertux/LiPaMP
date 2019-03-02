/* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
   distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

   Copyright 2019 Gert Dewit <gert@hobbiton.be>
*/
package be.hobbiton.maven.lipamp.common;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

public interface Packager {
    void write(File outputFile) throws MojoExecutionException;
}
