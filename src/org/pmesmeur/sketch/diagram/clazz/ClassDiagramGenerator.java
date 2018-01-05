package org.pmesmeur.sketch.diagram.clazz;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.VisibilityUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;


public class ClassDiagramGenerator {
    private final OutputStream outputStream;
    private final Project project;
    private final Module module;
    private final Set<PsiClass> managedPsiClasses;
    private final List<String> patternsToExclude;
    private final List<String> packages;


    public static ClassDiagramGenerator.Builder newBuilder(OutputStream outputStream,
                                                           Project project,
                                                           Module module) {
        return new ClassDiagramGenerator.Builder(outputStream, project, module);
    }



    public static class Builder {
        private final OutputStream outputStream;
        private final Project project;
        private final Module module;
        private final List<String> patternsToExclude;


        public Builder(OutputStream outputStream, Project project, Module module) {
            this.outputStream = outputStream;
            this.project = project;
            this.module = module;
            this.patternsToExclude = new ArrayList<String>();
        }


        public Builder exclude(String patternToExclude) {
            patternsToExclude.add(patternToExclude);
            return this;
        }


        public ClassDiagramGenerator build() {
            return new ClassDiagramGenerator(this);
        }

    }



    protected ClassDiagramGenerator(Builder builder) {
        this.outputStream = builder.outputStream;
        this.project = builder.project;
        this.module = builder.module;
        this.patternsToExclude = builder.patternsToExclude;
        this.packages = new ArrayList<String>();
        this.managedPsiClasses = computeManagedPsiClasses();
    }



    private Set<PsiClass> computeManagedPsiClasses() {
        Finder finder = new Finder(project, module, patternsToExclude);

        Set<PsiClass> pfiles = finder.getClasses();

        Set<String> packageSet = finder.getPackages();
        for (String packag : packageSet) {
            packages.add(packag);
        }
        Collections.sort(packages, new StringLengthComparator());

        return pfiles;
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
        int nbPackage = 0;
        String packageName = ((PsiJavaFile) clazz.getContainingFile()).getPackageName();
        for (String packag : packages) {
            if (packageName.startsWith(packag)) {
                write("package " + packag + " {");
                nbPackage++;
            }
        }

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

        while (nbPackage-- > 0) {
            write("}");
        }

    }



    private void declareClassMembers(PsiClass clazz) {
        declareClassAttributes(clazz);
        declareClassMethods(clazz);
    }



    private void declareClassAttributes(PsiClass clazz) {
        for (PsiField field : clazz.getAllFields()) {
            if (!typeBelongsToCurrentProject(field.getType()) &&
                    field.getContainingClass().equals(clazz)) {
                declareClassField(field);
            }
        }
    }



    private boolean typeBelongsToCurrentProject(PsiType type) {
        PsiClass typeClass = PsiTypesUtil.getPsiClass(type);
        return typeClass != null && typeClass.getProject().equals(project);
    }



    private void declareClassField(PsiField field) {
        String visibility = getVisibility(field.getModifierList());
        String fieldType = field.getType().getInternalCanonicalText();
        String fieldDeclaration = visibility + " " + field.getName() + " : " + fieldType;

        if (field.hasModifierProperty(PsiModifier.STATIC)) {
            fieldDeclaration = "{static} " + fieldDeclaration;
        }

        write("  " + fieldDeclaration);
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



    private void declareClassMethods(PsiClass clazz) {
        for (PsiMethod method : clazz.getAllMethods()) {
            if (method.getContainingClass().equals(clazz)) {
                declareClassMethod(method);
            }
        }
    }



    private void declareClassMethod(PsiMethod method) {
        String visibility = getVisibility(method.getModifierList());
        String methodDeclaration = "  " + visibility + " " + method.getName() + "()";

        if (method.hasModifierProperty(PsiModifier.ABSTRACT)) {
            methodDeclaration = "{abstract} " + methodDeclaration;
        }

        if (method.hasModifierProperty(PsiModifier.STATIC)) {
            methodDeclaration = "{static} " + methodDeclaration;
        }

        write(methodDeclaration);
    }



    private void declareClassRelationships(PsiClass clazz) {
        declareClassInheritence(clazz);
        declareClassAssociations(clazz);
    }



    private void declareClassInheritence(PsiClass clazz) {
        PsiClass superClass = clazz.getSuperClass();
        if (superClass != null) {
            write(clazz.getName() + " -up--|> " + superClass.getName());
        }
    }



    private void declareClassAssociations(PsiClass clazz) {
        for (PsiField field : clazz.getAllFields()) {
            if (typeBelongsToCurrentProject(field.getType()) &&
                    field.getContainingClass().equals(clazz) &&
                    !field.hasModifierProperty(PsiModifier.STATIC)) {
                write(clazz.getName() + " o-- " + field.getType().getPresentableText() + " : " + field.getName());
            }
        }
    }



    private class StringLengthComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o1.length() - o2.length();
        }

    }

}
