import java.lang.foreign.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

public class Main {
    public static final AddressLayout C_POINTER = ValueLayout.ADDRESS
            .withTargetLayout(MemoryLayout.sequenceLayout(java.lang.Long.MAX_VALUE, JAVA_BYTE));

    public static void main(String[] args) throws Throwable {
        System.out.println("release dll file");
        String rn;
        if (System.getproperty("os.name").toLowerCase().contains("windows")) {
            rn = "aa-windows-amd64.dll";
        } else {
            rm = "aa-linux-amd64.so";
        }
        var created = Files.createTempDirectory("aa-").resolve(rn);
        try (var res = Main.class.getClassLoader().getResourceAsStream(rn);
             var out = Files.newOutputStream(created, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            assert res != null;
            res.transferTo(out);
        }
        System.load(created.toAbsolutePath().toString());
        var linker = Linker.nativeLinker();
        var lookup = linker.defaultLookup().or(SymbolLookup.loaderLookup());
        var f = lookup.find("DoPrintHelloWorld").orElseThrow();
        var mh = linker.downcallHandle(f, FunctionDescriptor.of(C_POINTER));
        var r = (MemorySegment) mh.invokeExact();
        var u = r.getString(0, StandardCharsets.UTF_8);
        System.out.println(u);
        if (!"Hello, world! From dynamic library.".equals(u)) {
            throw new RuntimeException("not match");
        }
        var free = linker.downcallHandle(lookup.find("free").orElseThrow(), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        free.invokeExact(r);
    }
}
