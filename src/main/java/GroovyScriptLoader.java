import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;

public class GroovyScriptLoader {

    public static void main(String[] args) {
        CompilerConfiguration configuration = createBaseCompilerConfiguration(DefaultScript.class);
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        GroovyClassLoader gcl = new GroovyClassLoader(systemClassLoader, configuration, false);
        GroovyCodeSource codeSource = new GroovyCodeSource(scriptText, "build_xxx", "/groovy/script");
        Class scriptClass = gcl.parseClass(codeSource, false);
        try {
            DefaultScript script = (DefaultScript) scriptClass.newInstance();
            script.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static CompilerConfiguration createBaseCompilerConfiguration(Class<? extends Script> scriptBaseClass) {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setScriptBaseClass(scriptBaseClass.getName());
        configuration.setTargetBytecode(CompilerConfiguration.JDK8);
        configuration.setTargetDirectory(new File("."));
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
