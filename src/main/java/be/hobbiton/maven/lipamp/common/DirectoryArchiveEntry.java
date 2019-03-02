/* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
   distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

   Copyright 2019 Gert Dewit <gert@hobbiton.be>
*/
package be.hobbiton.maven.lipamp.common;

public class DirectoryArchiveEntry extends ArchiveEntry {
    private static final String DIR_SUFFIX = "/";

    public DirectoryArchiveEntry(String name, String userName, String groupName, int mode) {
        super(name.endsWith(DIR_SUFFIX) ? name : name + DIR_SUFFIX, null, userName, groupName, mode, ArchiveEntryType.D);
        setSize(0);
    }
}
