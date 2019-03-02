/* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
   distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

   Copyright 2019 Gert Dewit <gert@hobbiton.be>
*/
package be.hobbiton.maven.lipamp.common;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Misc constants
 *
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 */
public final class Constants {
    private Constants() {
        throw new IllegalStateException("Constants class");
    }
    public static final char ARTIFACT_SEPARATOR = '-';
    public static final char GROUP_SEPARATOR = '.';
    public static final String DOT = ".";
    public static final String SLASH = "/";
    public static final String DEFAULT_USERNAME = "root";
    public static final String DEFAULT_GROUPNAME = "root";
    public static final String DEFAULT_FILEMODE = "0644";
    public static final int DEFAULT_FILEMODE_VALUE = Integer.parseInt(DEFAULT_FILEMODE, 8);
    public static final String DEFAULT_DIRMODE = "0755";
    public static final int DEFAULT_DIRMODE_VALUE = Integer.parseInt(DEFAULT_DIRMODE, 8);
    public static final int INVALID_MODE = -1;
    public static final long INVALID_SIZE = -1L;
    private static final String CURRENT_PATH_ELEM = DOT;
    public static final Path CURRENT_PATH = Paths.get(CURRENT_PATH_ELEM);
    public static final Path ROOT_PATH = Paths.get(SLASH);
    public static final String CURRENT_DIR = "./";
    public static final Path CURRENT_DIR_PATH = Paths.get(CURRENT_DIR);
}
