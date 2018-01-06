package org.pmesmeur.sketch.diagram.clazz;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.pmesmeur.sketch.diagram.JavaFileFinder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Finder {

    private final Project project;
    private final Module module;
    private Set<PsiClass> classes;
    private Set<String> packages;
    private final List<String> patternsToExclude;


    public Finder(Project project, Module module, List<String> patternsToExclude) {
        this.project = project;
        this.module = module;
        this.patternsToExclude = patternsToExclude;
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
        String fileDirectory = file.getParent().toString();
        String moduleDirectory = module.getModuleFile().getParent().toString();

        String dir = fileDirectory.replace(moduleDirectory, "");

        return excluded(dir);
    }



    private boolean excluded(String dirName) {
        for (String patternToExclude : this.patternsToExclude) {
            if (dirName.contains(patternToExclude)) {
                return true;
            }
        }

        return false;
    }

}
