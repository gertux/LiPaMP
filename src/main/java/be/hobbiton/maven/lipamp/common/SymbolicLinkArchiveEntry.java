package be.hobbiton.maven.lipamp.common;

/**
 * @author <a href="mailto:gert@hobbiton.be">Gert Dewit</a>
 */
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
