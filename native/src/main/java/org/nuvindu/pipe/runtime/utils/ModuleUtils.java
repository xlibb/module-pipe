package org.nuvindu.pipe.runtime.utils;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Module;

/**
 * This class includes the utility functions related to the Pipe module.
 */
public class ModuleUtils {

    private static Module module;

    private ModuleUtils() {}

    public static void setModule(Environment environment) {
        module = environment.getCurrentModule();
    }

    public static Module getModule() {
        return module;
    }
}
