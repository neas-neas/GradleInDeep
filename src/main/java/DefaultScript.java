import groovy.lang.Closure;
import groovy.lang.Script;

public abstract class DefaultScript extends Script {

    public void buildscript(Closure configureClosure) {
        println("DefaultScript:buildscript  parameters:" + configureClosure.getMaximumNumberOfParameters());
        configureClosure.call();
    }

}
