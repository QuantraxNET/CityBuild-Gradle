package net.quantrax.citybuild.global;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GlobalMethods {
    public static String[] findListenerPaths(String rootPath) {
        List<String> listenerPaths = new ArrayList<>();
        findListenerPathsRecursive(new File(rootPath), listenerPaths);
        return listenerPaths.toArray(new String[0]);
    }
    private static void findListenerPathsRecursive(File directory, List<String> listenerPaths) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        findListenerPathsRecursive(file, listenerPaths);
                    } else if (file.getAbsolutePath().toLowerCase().contains("listener")) {
                        listenerPaths.add(file.getAbsolutePath());
                    }
                }
            }
        }
    }
}
