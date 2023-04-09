import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class GroovyScriptLoader {

    public static void main(String[] args) {
        File outputDirectory = new File(".");
        String scriptName = "build_xxx";
        CompilerConfiguration configuration = createBaseCompilerConfiguration(DefaultScript.class, outputDirectory);
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        GroovyClassLoader gcl = new GroovyClassLoader(systemClassLoader, configuration, false);
        GroovyCodeSource codeSource = new GroovyCodeSource(scriptText, scriptName, "/groovy/script");
        Class scriptClass = gcl.parseClass(codeSource, false);
        try {
            URL url = outputDirectory.toURI().toURL();
            URL[] urls = new URL[]{ url };
            URLClassLoader classloader = new URLClassLoader(urls);
            Class cls = classloader.loadClass(scriptName).asSubclass(DefaultScript.class);
            DefaultScript script = (DefaultScript) cls.getDeclaredConstructor().newInstance();;
            script.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static CompilerConfiguration createBaseCompilerConfiguration(Class<? extends Script> scriptBaseClass, File outputDirectory) {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setScriptBaseClass(scriptBaseClass.getName());
        configuration.setTargetBytecode(CompilerConfiguration.JDK8);
        configuration.setTargetDirectory(outputDirectory);
        return configuration;
    }

    /**
     * buildscript {
     *     println 'test'
     * }
     */
    private static String scriptText =
        "buildscript {" +
        "    println 'test'" +
        "}";

}
