package org.pmesmeur.sketchit.diagram.clazz;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.*;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;
import org.pmesmeur.sketchit.diagram.JavaFileFinder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Finder {

    private final Project project;
    private final Module module;
    private Set<PsiClass> classes;
    private Set<String> packages;


    public Finder(Project project, Module module) {
        this.project = project;
        this.module = module;
        this.packages = new HashSet<String>();
        this.classes = findClasses();
    }



    public Set<PsiClass> getClasses() {
        return classes;
    }



    public Set<String> getPackages() {
        return packages;
    }



    private Set<PsiClass> findClasses() {
        JavaFileFinder javaFileFinder = new JavaFileFinder(project, module);

        return computeManagedPsiClassesFromFiles(javaFileFinder.getFoundFiles());
    }



    private Set<PsiClass> computeManagedPsiClassesFromFiles(List<PsiJavaFile> pfiles) {
        Set<PsiClass> managedPsiClasses = new HashSet<PsiClass>();

        for (PsiJavaFile file : pfiles) {
            recordFilePackageAsKnownPackage(file);

            if (!isTestFile(file)) {
                PsiClass[] classes = file.getClasses();
                for (PsiClass clazz : classes) {
                    managedPsiClasses.add(clazz);
                }
            }
        }

        return managedPsiClasses;
    }



    private void recordFilePackageAsKnownPackage(PsiJavaFile local) {
        packages.add(local.getPackageName());
    }




    private boolean isTestFile(PsiFile file) {
        return ModuleRootManager.getInstance(module)
                                .getFileIndex()
                                .isUnderSourceRootOfType(file.getVirtualFile(),
                                                         JavaModuleSourceRootTypes.TESTS);
    }

}
