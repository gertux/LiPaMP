/* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
   distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

   Copyright 2019 Gert Dewit <gert@hobbiton.be>
*/
package be.hobbiton.maven.lipamp.plugin;

import be.hobbiton.maven.lipamp.common.LinuxPackagingException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 */
public class LinkEntry extends Attributable{
    private String path;
    private String target;

    public LinkEntry() {
    }

    public LinkEntry(String path, String target, String username, String groupname, String mode) {
        this.path = path;
        this.target = target;
        setUsername(username);
        setGroupname(groupname);
        setMode(mode);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean isValid() {
        if (StringUtils.isNotBlank(this.path) && StringUtils.isNotBlank(this.target)) {
            return true;
        }
        throw new LinuxPackagingException(String.format("Invalid LinkEntry %s -> %s", this.path, this.target));
    }

    @Override
    public String toString() {
        return String.format("%s -> %s u=%s g=%s m=%s", this.path, this.target, getUsername(), getGroupname(), getMode());
    }
}
