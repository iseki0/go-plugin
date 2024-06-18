package space.iseki.goplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class GoCompilePlugin implements Plugin<Project> {
    @Override
    public void apply(@NotNull Project target) {
        target.getExtensions().add("go", new GoCompileExtension(target));
    }
}
