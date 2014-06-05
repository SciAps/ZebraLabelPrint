package com.sciaps.android.zebralabelprint.zebraprint.utils;

/**
 * Created by MonkeyFish on 2/24/14.
 */
public  class DecimalRounder {
    public static String roundWPercent(float f){
        if (f>10){
            return String.format("%.1f%%", f);


        }
        if (f>1){
            return String.format("%.2f%%", f);

        }
        return String.format("%.3f%%", f);

    }

    public static String roundWPercent(double d){
        if (d>10){
            return String.format("%.1f%%", d);


        }
        if (d>1){
            return String.format("%.2f%%", d);

        }
        return String.format("%.3f%%", d);
    }
    public static String round(float f){
        if (f>10){
            return String.format("%.2f", f);


        }
        if (f>1){
            return String.format("%.2f", f);

        }
        return String.format("%.3f", f);

    }
    public static String round(double d){
        if (d>10){
            return String.format("%.2f", d);


        }
        if (d>1){
            return String.format("%.2f", d);

        }
        return String.format("%.3f", d);

    }
}
