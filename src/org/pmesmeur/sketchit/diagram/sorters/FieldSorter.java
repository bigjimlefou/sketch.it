package org.pmesmeur.sketchit.diagram.sorters;

import com.intellij.psi.PsiField;

import java.util.*;


public class FieldSorter {

    static public List<PsiField> sort(PsiField [] fields) {
        List<PsiField> fieldList = Arrays.asList(fields);
        Collections.sort(fieldList, new PsiFieldComparator());

        return fieldList;

    }



    private static class PsiFieldComparator implements Comparator<PsiField> {

        @Override
        public int compare(PsiField field1, PsiField field2) {
            String name1 = field1.getName();
            String name2 = field2.getName();

            return name1.compareTo(name2) ;
        }

    }

}
