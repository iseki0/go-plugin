package space.iseki.goplugin;

import java.io.Serial;
import java.io.Serializable;

public enum BuildMode implements Serializable {
    CShared("c-shared"),
    Default("default"),
    ;
    @Serial
    private static final long serialVersionUID = 1L;

    public final String arg;

    BuildMode(String arg) {
        this.arg = arg;
    }
}
