package org.pmesmeur.sketch.diagram.clazz;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.pmesmeur.sketch.diagram.ClassFileFinder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Finder {

    private final Project project;
    private final Module module;
    private Set<PsiClass> classes;
    private Set<String> packages;
    private Set<String> sourceDirectories;
    private final List<String> patternsToExclude;


    public Finder(Project project, Module module, List<String> patternsToExclude) {
        this.project = project;
        this.module = module;
        this.patternsToExclude = patternsToExclude;
        this.packages = new HashSet<String>();
        this.sourceDirectories = new HashSet<String>();
        this.classes = findClasses();
    }



    public Set<PsiClass> getClasses() {
        return classes;
    }



    public Set<String> getPackages() {
        return packages;
    }



    public Set<String> getSourceDirectories() {
        return sourceDirectories;
    }



    private Set<PsiClass> findClasses() {
        ClassFileFinder classFileFinder = new ClassFileFinder(project, module);

        return computeManagedPsiClassesFromFiles(classFileFinder.findFiles());
    }



    private Set<PsiClass> computeManagedPsiClassesFromFiles(List<PsiFile> pfiles) {
        Set<PsiClass> managedPsiClasses = new HashSet<PsiClass>();

        for (PsiFile file : pfiles) {
            if (file instanceof PsiJavaFile) {
                recordFilePackageAsKnownPackage((PsiJavaFile) file);
            }

            if (file instanceof PsiClassOwner && !isTestFile(file)) {
                sourceDirectories.add(file.getParent().getVirtualFile().getPath());

                PsiClass[] classes = ((PsiClassOwner) file).getClasses();
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
