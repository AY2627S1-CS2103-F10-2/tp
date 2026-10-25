package seedu.address.testutil;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Set;

/** Loads selected application classes afresh with a controlled application location. */
public class ApplicationClassLoader extends ClassLoader {
    private final URL location;
    private final Set<String> isolatedClasses;

    /** Creates a loader that assigns the given location to the selected classes. */
    public ApplicationClassLoader(URL location, String... isolatedClasses) {
        super(ApplicationClassLoader.class.getClassLoader());
        this.location = location;
        this.isolatedClasses = Set.of(isolatedClasses);
    }

    /** Simulates a launcher supplying a URL with an unescaped space, which is not a valid URI. */
    @SuppressWarnings("deprecation")
    public static URL invalidLocation() throws MalformedURLException {
        return new URL("file:/invalid application location/");
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (!isolatedClasses.contains(name)) {
            return super.loadClass(name, resolve);
        }
        Class<?> loaded = findLoadedClass(name);
        if (loaded == null) {
            String resource = name.replace('.', '/') + ".class";
            try (InputStream input = getParent().getResourceAsStream(resource)) {
                if (input == null) {
                    throw new ClassNotFoundException(name);
                }
                byte[] bytes = input.readAllBytes();
                ProtectionDomain domain = new ProtectionDomain(new CodeSource(location, (Certificate[]) null), null);
                loaded = defineClass(name, bytes, 0, bytes.length, domain);
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }
        if (resolve) {
            resolveClass(loaded);
        }
        return loaded;
    }
}
