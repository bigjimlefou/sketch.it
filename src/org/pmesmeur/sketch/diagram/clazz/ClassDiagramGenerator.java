package org.pmesmeur.sketch.diagram.clazz;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ClassDiagramGenerator {
    private final OutputStream outputStream;
    private final Project project;
    private final Module module;


    public ClassDiagramGenerator(OutputStream outputStream, Project project, Module module) {
        this.outputStream = outputStream;
        this.project = project;
        this.module = module;
    }



    public void generate()
    {
        write("@startuml");
        write("");

        String msg = "Module '" + module.getModuleFilePath() + '\'';

        List<PsiFile> pfiles = findFiles();
        handleFiles(msg, pfiles);

        write("");
        write("@enduml");
    }



    private void write(String s) {
        String dataToWrite = s + "\n";
        try {
            outputStream.write(dataToWrite.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @NotNull
    private List<PsiFile> findFiles() {
        List<PsiFile> pfiles = new ArrayList<PsiFile>();
        findFiles(module, pfiles);
        return pfiles;
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



    private void findFiles(List<PsiFile> files, PsiDirectory directory, boolean subdirs)
    {
        PsiFile[] locals = directory.getFiles();
        for (PsiFile local : locals)
        {
            if (psiFileBelongsToCurrentModule(local)) {
                files.add(local);
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



    private void handleFiles(String msg, List<PsiFile> pfiles) {
        for (PsiFile file : pfiles) {
            if (file instanceof PsiClassOwner) {
                handlePsiClassOwner(msg, (PsiClassOwner) file);
            }
        }
    }



    private void handlePsiClassOwner(String msg, PsiClassOwner file) {
        //System.out.println(msg + ": " + file.getName() + " (" + file.getFileType().getName() + ")");
        PsiClass psiClasses [] = file.getClasses();

        for (PsiClass psiClass : psiClasses) {
            if (psiClass.isInterface()) {
                write("interface " + psiClass.getName());
            } else if (psiClass.isEnum()) {
                write("enum " + psiClass.getName());
            } else if (psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
                write("class " + psiClass.getName());
            } else {
                write("abstract class " + psiClass.getName());
            }
        }
    }

}
