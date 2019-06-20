package org.pmesmeur.sketchit.diagram.sorters;

import com.intellij.psi.PsiMethod;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MethodSorter {


    static public List<PsiMethod> sort(PsiMethod [] methods) {
        List<PsiMethod> methodList = Arrays.asList(methods);
        Collections.sort(methodList, new PsiMethodComparator());

        return methodList;

    }



    private static class PsiMethodComparator implements Comparator<PsiMethod> {

        @Override
        public int compare(PsiMethod method1, PsiMethod method2) {
            String name1 = method1.getName();
            String name2 = method2.getName();

            return name1.compareTo(name2);
        }

    }

}
