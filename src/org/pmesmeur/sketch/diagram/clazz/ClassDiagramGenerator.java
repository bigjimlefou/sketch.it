package org.pmesmeur.sketch.diagram.clazz;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.VisibilityUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassDiagramGenerator {
    private final OutputStream outputStream;
    private final Project project;
    private final Module module;
    private final Set<PsiClass> managedPsiClasses;


    public ClassDiagramGenerator(OutputStream outputStream, Project project, Module module) {
        this.outputStream = outputStream;
        this.project = project;
        this.module = module;
        this.managedPsiClasses = computeManagedPsiClasses();
    }



    private Set<PsiClass> computeManagedPsiClasses() {
        List<PsiFile> pfiles = findFiles();
        return computeManagedPsiClassesFromFiles(pfiles);
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



    private Set<PsiClass> computeManagedPsiClassesFromFiles(List<PsiFile> pfiles) {
        Set<PsiClass> managedPsiClasses = new HashSet<PsiClass>();

        for (PsiFile file : pfiles) {
            if (file instanceof PsiClassOwner) {
                PsiClass[] classes = ((PsiClassOwner) file).getClasses();
                for (PsiClass clazz : classes) {
                    managedPsiClasses.add(clazz);
                }
            }
        }

        return managedPsiClasses;
    }



    public void generate()
    {
        write("@startuml");
        write("");

        for (PsiClass clazz : managedPsiClasses) {
            declareClass(clazz);
            write("\n");
        }

        write("\n\n");

        for (PsiClass clazz : managedPsiClasses) {
            declareClassRelationships(clazz);
        }

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



    private void declareClass(PsiClass clazz) {
        if (clazz.isEnum()) {
            write("enum " + clazz.getName());
        } else {
            if (clazz.isInterface()) {
                write("interface " + clazz.getName() + " {");
            } else if (clazz.hasModifierProperty(PsiModifier.ABSTRACT)) {
                write("abstract class " + clazz.getName() + " {");
            } else {
                write("class " + clazz.getName() + " {");
            }
            declareClassMembers(clazz);
            write("}");
        }
    }



    private void declareClassMembers(PsiClass clazz) {
        declareClassAttributes(clazz);
        declareClassMethods(clazz);
    }



    private void declareClassAttributes(PsiClass clazz) {

    }



    private void declareClassMethods(PsiClass clazz) {
        for (PsiMethod method : clazz.getAllMethods()) {
            if (method.getContainingClass().equals(clazz)) {
                declareClassMethod(method);
            }
        }
    }



    private void declareClassMethod(PsiMethod method) {
        String visibility = getVisibility(method.getModifierList());
        write("  " + visibility + " " + method.getName() + "()");
    }



    @NotNull
    private String getVisibility(PsiModifierList methodModifiers) {
        String visibility = VisibilityUtil.getVisibilityModifier(methodModifiers);

        if (visibility == "public") {
            visibility = "+";
        } else if (visibility == "protected") {
            visibility = "#";
        } else if (visibility == "private") {
            visibility = "-";
        } else if (visibility == "packageLocal") {
            visibility = "~";
        }

        return visibility;
    }



    private void declareClassRelationships(PsiClass clazz) {
        declareClassInheritence(clazz);
        declareClassAssociations(clazz);
    }



    private void declareClassInheritence(PsiClass clazz) {
        PsiClass superClass = clazz.getSuperClass();
        if (superClass != null) {
            write(clazz.getName() + " --|> " + superClass.getName());
        }
    }



    private void declareClassAssociations(PsiClass clazz) {
        PsiField[] allFields = clazz.getAllFields();
        for (PsiField field : allFields) {
            if (!field.hasModifierProperty(PsiModifier.STATIC) && field instanceof PsiClass) {
                PsiClass psiClass = (PsiClass) field;
                write(psiClass.getName() + " o-- " + psiClass.getName());
            }
        }
    }

}
