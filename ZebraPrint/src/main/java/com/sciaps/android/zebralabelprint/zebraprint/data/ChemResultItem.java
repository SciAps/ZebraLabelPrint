package com.sciaps.android.zebralabelprint.zebraprint.data;

import com.sciaps.common.ChemResult;
import com.sciaps.common.data.libs.GradeRange;

import org.apache.commons.lang.math.DoubleRange;

import java.util.Comparator;

/**
* Created by sweiss on 6/5/14.
*/
public class ChemResultItem {
    public ChemResult chemResult;
    public DoubleRange gradeRange;

    public GradeRange getGradeRange() {
        GradeRange retval = new GradeRange(chemResult.element, gradeRange.getMinimumFloat(), gradeRange.getMaximumFloat());
        return retval;
    }

    public static Comparator<ChemResultItem> ConcentrationDecend = new Comparator<ChemResultItem>() {
        @Override
        public int compare(ChemResultItem o1, ChemResultItem o2) {
            return Float.compare(o2.chemResult.value, o1.chemResult.value);
        }
    };

    public static Comparator<ChemResultItem> ConcentrationAscend = new Comparator<ChemResultItem>() {
        @Override
        public int compare(ChemResultItem o1, ChemResultItem o2) {
            return Float.compare(o1.chemResult.value, o2.chemResult.value);
        }
    };

    public static Comparator<ChemResultItem> AtomicDecend = new Comparator<ChemResultItem>() {
        @Override
        public int compare(ChemResultItem o1, ChemResultItem o2) {
            return o1.chemResult.element.atomicNumber - o2.chemResult.element.atomicNumber;
            //return Integer.compare(o1.chemResult.element.atomicNumber, o2.chemResult.element.atomicNumber);
        }
    };

    public static Comparator<ChemResultItem> AtomicAscend = new Comparator<ChemResultItem>() {
        @Override
        public int compare(ChemResultItem o1, ChemResultItem o2) {
            return o2.chemResult.element.atomicNumber - o1.chemResult.element.atomicNumber;
            //return Integer.compare(o2.chemResult.element.atomicNumber, o1.chemResult.element.atomicNumber);
        }
    };

    public static Comparator<ChemResultItem> AlphaDecend = new Comparator<ChemResultItem>() {
        @Override
        public int compare(ChemResultItem o1, ChemResultItem o2) {
            return o1.chemResult.element.name().compareTo(o2.chemResult.element.name());
        }
    };

    public static Comparator<ChemResultItem> AlphaAscend = new Comparator<ChemResultItem>() {
        @Override
        public int compare(ChemResultItem o1, ChemResultItem o2) {
            return o2.chemResult.element.name().compareTo(o1.chemResult.element.name());
        }
    };
}
