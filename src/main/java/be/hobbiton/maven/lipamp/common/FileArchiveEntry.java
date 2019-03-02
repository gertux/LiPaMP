/* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
   distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

   Copyright 2019 Gert Dewit <gert@hobbiton.be>
*/
package be.hobbiton.maven.lipamp.common;

import java.io.File;

public class FileArchiveEntry extends ArchiveEntry {

    public FileArchiveEntry(String name, File file, String userName, String groupName, int mode) {
        super(name, file, userName, groupName, mode, ArchiveEntryType.F);
        if (file != null && file.isFile()) {
            setSize(file.length());
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (getFile() != null && getFile().isFile());
    }
}