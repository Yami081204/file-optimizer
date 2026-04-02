package com.fileoptimizer.core.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class PluginManager {

    private final List<Plugin> loadedPlugins = new ArrayList<>();

    public void loadPlugins(String pluginDir) {
        File dir = new File(pluginDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".jar"));
        if (files == null) return;

        for (File file : files) {
            try {
                URL jarUrl = file.toURI().toURL();
                URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl});
                
                ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, classLoader);
                for (Plugin plugin : serviceLoader) {
                    plugin.init();
                    loadedPlugins.add(plugin);
                }
            } catch (Exception e) {
            }
        }
    }

    public List<String> executeAll(List<com.fileoptimizer.common.model.FileMetadata> files) {
        List<String> results = new ArrayList<>();
        for (Plugin plugin : loadedPlugins) {
            try {
                String result = plugin.execute(files);
                results.add(plugin.getName() + ": " + result);
            } catch (Exception e) {
                results.add(plugin.getName() + " failed: " + e.getMessage());
            }
        }
        return results;
    }

    public List<Plugin> getLoadedPlugins() {
        return loadedPlugins;
    }
}
