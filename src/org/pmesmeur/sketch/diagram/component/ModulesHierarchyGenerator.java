package org.pmesmeur.sketch.diagram.component;

import com.intellij.openapi.module.Module;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class ModulesHierarchyGenerator {
    private final List<ModulePath> modulePaths = new ArrayList<ModulePath>();
    Set<ModulePath> modulePathsDone = new HashSet<ModulePath>();


    public ModulesHierarchyGenerator(Set<Module> managedModules) {
        buildModulePaths(managedModules);
    }



    private void buildModulePaths(Set<Module> managedModules) {
        for (Module module : managedModules) {
            modulePaths.add(new ModulePath(module));
        }
        Collections.sort(modulePaths, new ModulePathComparator());
        modulePaths.remove(modulePaths.size() - 1); /// remove last element, i.e. project-root
        buildModulePathsDependencies();
    }



    private void buildModulePathsDependencies() {
        for (int i = 0 ; i < modulePaths.size() ; i++) {
            for (int j = i + 1 ; j < modulePaths.size() ; j++) {
                ModulePath containedModulePath = modulePaths.get(i);
                ModulePath containingModulePath = modulePaths.get(j);

                if (containedModulePath.path.contains(containingModulePath.path)) {
                    containingModulePath.subModules.add(containedModulePath);
                    break;
                }
            }
        }
    }



    public void generate(OutputStream outputStream) {
        for (int i = modulePaths.size() - 1 ; i >= 0 ; --i) {
            ModulePath modulePath = modulePaths.get(i);

            generate(outputStream, modulePath);
        }
    }



    private void generate(OutputStream outputStream, ModulePath modulePath) {
        if (!modulePathsDone.contains(modulePath)) {
            modulePathsDone.add(modulePath);
            if (modulePath.subModules.size() > 0) {
                write(outputStream, "component \"" + modulePath.module.getName() + "\" {");

                for (ModulePath subModulePath : modulePath.subModules) {
                    write(outputStream, "    [" + subModulePath.module.getName() + "]");
                }

                for (ModulePath subModulePath : modulePath.subModules) {
                    generate(outputStream, subModulePath);
                }

                write(outputStream, "}\n\n");
            }
        }
    }



    private void write(OutputStream outputStream, String s) {
        String dataToWrite = s + "\n";
        try {
            outputStream.write(dataToWrite.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static class ModulePath {
        private final Module module;
        private final String path;
        public ArrayList<ModulePath> subModules = new ArrayList<ModulePath>();


        private ModulePath(Module module) {
            this.module = module;
            this.path = getModuleFilePath();
        }


        private String getModuleFilePath() {
            File file = new File(module.getModuleFilePath());
            return file.getParent();
        }

    }



    private static class ModulePathComparator implements Comparator<ModulePath> {

        @Override
        public int compare(ModulePath o1, ModulePath o2) {
            return o2.path.length() - o1.path.length();
        }

    }

}
