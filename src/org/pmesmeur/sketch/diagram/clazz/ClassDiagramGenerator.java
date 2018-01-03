package org.pmesmeur.sketch.diagram.clazz;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;

public class ClassDiagramGenerator {
    private final Project project;


    public ClassDiagramGenerator(Project project) {

        this.project = project;
    }



    public void generate() {
        System.out.println("Class Diagram Generator");
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        for (Module module : moduleManager.getModules()) {
            process(module);
        }
    }


    private void process(Module module)
    {
        List<PsiFile> pfiles = new ArrayList<PsiFile>();
        findFiles(module, pfiles);
        String msg = "Module '" + module.getModuleFilePath() + '\'';
        handleFiles(msg, pfiles);
    }



    private void handleFiles(String msg, List<PsiFile> pfiles) {
        for (PsiFile file : pfiles) {
            if (file instanceof PsiClassOwner) {
                handlePsiClassOwner(msg, (PsiClassOwner) file);
            }
        }
    }



    private void handlePsiClassOwner(String msg, PsiClassOwner file) {
        System.out.println(msg + ": " + file.getName() + " (" + file.getFileType().getName() + ")");
        PsiClass psiClasses [] = file.getClasses();

        for (PsiClass psiClass : psiClasses) {
            System.out.println("=> " + psiClass.getName());
        }
    }



    private void findFiles(Project project, List<PsiFile> files)
    {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules)
        {
            findFiles(module, files);
        }

    }

    private void findFiles(Module module, List<PsiFile> files)
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


    private static void findFiles(List<PsiFile> files, PsiDirectory directory, boolean subdirs)
    {
        PsiFile[] locals = directory.getFiles();
        for (PsiFile local : locals)
        {
            files.add(local);
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
}
