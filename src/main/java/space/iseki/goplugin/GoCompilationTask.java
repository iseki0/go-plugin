package space.iseki.goplugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

@CacheableTask
public class GoCompilationTask extends DefaultTask {
    static final Map<String, String> DEFAULT_ENVIRONMENT = Map.of("CGO_ENABLED", "0");
    private final Property<String> goCmd = getProject().getObjects().property(String.class).convention("go");
    private final DirectoryProperty inputSourceProperty = getProject().getObjects().directoryProperty();
    private final Property<GoTarget> goTargetProperty = getProject().getObjects().property(GoTarget.class);
    private final RegularFileProperty outputFileProperty = getProject().getObjects().fileProperty();
    private final ListProperty<String> buildTagsProperty = getProject().getObjects().listProperty(String.class).convention(Collections.emptyList());
    private final MapProperty<String, String> environmentProperty = getProject().getObjects().mapProperty(String.class, String.class).convention(DEFAULT_ENVIRONMENT);
    private final Property<Boolean> trimPathProperty = getProject().getObjects().property(Boolean.class).convention(true);
    private final Property<String> packageProperty = getProject().getObjects().property(String.class).convention(".");
    private final Property<String> ldflagsProperty = getProject().getObjects().property(String.class).convention("-s -w -buildid=");
    private final ListProperty<String> freeArgumentsProperty = getProject().getObjects().listProperty(String.class).convention(Collections.emptyList());
    private final Property<BuildMode> buildModeProperty = getProject().getObjects().property(BuildMode.class).convention(BuildMode.Default);


    @Input
    public Property<String> getGoCmd() {
        return goCmd;
    }

    @Input
    public Property<String> getPackageProperty() {
        return packageProperty;
    }

    @Input
    public Property<String> getLdflagsProperty() {
        return ldflagsProperty;
    }

    @Input
    public ListProperty<String> getBuildTagsProperty() {
        return buildTagsProperty;
    }

    @Input
    public MapProperty<String, String> getEnvironmentProperty() {
        return environmentProperty;
    }

    @Input
    public Property<Boolean> getTrimPathProperty() {
        return trimPathProperty;
    }

    @Input
    public ListProperty<String> getFreeArgumentsProperty() {
        return freeArgumentsProperty;
    }

    @OutputFile
    public RegularFileProperty getOutputFileProperty() {
        return outputFileProperty;
    }

    @InputDirectory
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public DirectoryProperty getInputSourceProperty() {
        return inputSourceProperty;
    }

    @Input
    public Property<GoTarget> getGoTargetProperty() {
        return goTargetProperty;
    }

    @Input
    public Property<BuildMode> getBuildModeProperty() {
        return buildModeProperty;
    }

    @TaskAction
    void doCompile() {
        var cmds = new ArrayList<String>();
        cmds.add(goCmd.get());
        cmds.add("build");
        cmds.add("-buildmode=" + buildModeProperty.get().arg);
        cmds.add("-o");
        cmds.add(outputFileProperty.get().getAsFile().getAbsolutePath());
        if (!buildTagsProperty.get().isEmpty()) {
            cmds.add("-tags");
            cmds.add(String.join(",", buildTagsProperty.get()));
        }
        if (!ldflagsProperty.get().isEmpty()) {
            cmds.add("-ldflags");
            cmds.add(ldflagsProperty.get());
        }
        if (trimPathProperty.get()) {
            cmds.add("-trimpath");
        }
        if (!freeArgumentsProperty.get().isEmpty()) {
            cmds.addAll(freeArgumentsProperty.get());
        }
        if (!packageProperty.get().isEmpty()) {
            cmds.add(packageProperty.get());
        }
        getLogger().info("cmd: {}", cmds);
        var pb = new ProcessBuilder(cmds);
        pb.directory(inputSourceProperty.get().getAsFile());
        pb.environment().put("GOOS", goTargetProperty.get().goos());
        pb.environment().put("GOARCH", goTargetProperty.get().goarch());
        pb.environment().putAll(environmentProperty.get());
        pb.redirectErrorStream(true);
        Process process;
        try {
            process = pb.start();
            process.getOutputStream().close();
            new BufferedReader(new InputStreamReader(process.getInputStream())).lines().forEach(s -> getLogger().warn("go: {}", s));
            process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("Go compilation failed", e);
        }
        if (process.exitValue() != 0) {
            throw new RuntimeException("Go compilation failed with exit code " + process.exitValue());
        }

    }
}

