/* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
   distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

   Copyright 2019 Gert Dewit <gert@hobbiton.be>
*/
package be.hobbiton.maven.lipamp.common;

public class SymbolicLinkArchiveEntry extends ArchiveEntry {
    private final String target;

    public SymbolicLinkArchiveEntry(String name, String target, String userName, String groupName, int mode) {
        super(name, null, userName, groupName, mode, ArchiveEntryType.S);
        this.target = target;
        setSize(0);
    }

    public String getTarget() {
        return target;
    }
}
