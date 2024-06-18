package space.iseki.goplugin;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

public record GoTarget(String goos, String goarch) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public GoTarget {
        if (goos == null) throw new IllegalArgumentException("goos == null");
        if (goarch == null) throw new IllegalArgumentException("goarch == null");
    }

    public void putIntoEnv(Map<String, String> env) {
        env.put("GOOS", goos);
        env.put("GOARCH", goarch);
    }
}