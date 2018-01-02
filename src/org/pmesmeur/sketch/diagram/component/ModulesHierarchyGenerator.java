package org.pmesmeur.sketch.diagram.component;

import com.intellij.openapi.module.Module;

import java.io.File;
import java.util.*;

public class ModulesHierarchyGenerator {
    private final List<ModulePath> modulePaths = new ArrayList<ModulePath>();


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

    Set<ModulePath> modulePathsDone = new HashSet<ModulePath>();

    public void generate() {
        for (int i = modulePaths.size() - 1 ; i >= 0 ; --i) {
            ModulePath modulePath = modulePaths.get(i);

            generate(modulePath);
        }
    }



    private void generate(ModulePath modulePath) {
        if (!modulePathsDone.contains(modulePath)) {
            modulePathsDone.add(modulePath);
            if (modulePath.subModules.size() > 0) {
                System.out.println("package \"" + modulePath.module.getName() + "\" {");

                for (ModulePath subModulePath : modulePath.subModules) {
                    System.out.println("    [" + subModulePath.module.getName() + "]");
                }

                for (ModulePath subModulePath : modulePath.subModules) {
                    generate(subModulePath);
                }

                System.out.println("}\n\n");
            }
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
