package org.pmesmeur.sketch.diagram.clazz;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

import java.util.ArrayList;
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
        List<PsiFile> pFiles = new ArrayList<PsiFile>();
        findFiles(pFiles);

        return computeManagedPsiClassesFromFiles(pFiles);
    }



    private void findFiles(List<PsiFile> files)
    {
        VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();
        for (VirtualFile file : roots)
        {
            PsiDirectory dir = PsiManager.getInstance(project).findDirectory(file);
            if (dir != null)
            {
                findFiles(files, dir, true);
            }
        }


    }




    private void findFiles(List<PsiFile> files, PsiDirectory directory, boolean subdirs)
    {
        PsiFile[] locals = directory.getFiles();
        for (PsiFile local : locals)
        {
            if (psiFileBelongsToCurrentModule(local)) {
                files.add(local);
                if (local instanceof PsiJavaFile) {
                    recordFilePackageAsKnownPackage((PsiJavaFile) local);
                }
            }
        }

        if (subdirs)
        {
            PsiDirectory[] dirs = directory.getSubdirectories();
            for (PsiDirectory dir : dirs)
            {
                findFiles(files, dir, subdirs);
            }

        }
    }



    private boolean psiFileBelongsToCurrentModule(PsiFile file) {
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        ProjectFileIndex projectFileIndex = projectRootManager.getFileIndex();
        Module fileModule = projectFileIndex.getModuleForFile(file.getVirtualFile());

        return module.equals(fileModule);
    }



    private void recordFilePackageAsKnownPackage(PsiJavaFile local) {
        packages.add(local.getPackageName());
    }




    private Set<PsiClass> computeManagedPsiClassesFromFiles(List<PsiFile> pfiles) {
        Set<PsiClass> managedPsiClasses = new HashSet<PsiClass>();

        for (PsiFile file : pfiles) {
            if (file instanceof PsiClassOwner && !isTestFile(file)) {
                PsiClass[] classes = ((PsiClassOwner) file).getClasses();
                for (PsiClass clazz : classes) {
                    managedPsiClasses.add(clazz);
                }
            }
        }

        return managedPsiClasses;
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
