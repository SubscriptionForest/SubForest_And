package com.example.subforest.ui;

import androidx.annotation.NonNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public final class DateUtils {
    private static final SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public static String periodLabel(@NonNull String startYmd, int repeatDays) {
        try {
            Date start = ISO.parse(startYmd); if (start==null) return "";
            Calendar c = Calendar.getInstance(); c.setTime(start);
            String from = (c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.DAY_OF_MONTH);
            c.add(Calendar.DAY_OF_YEAR, repeatDays);
            String to = (c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.DAY_OF_MONTH);
            return from + " - " + to;
        } catch (ParseException e) { return ""; }
    }

    public static String addDaysMd(@NonNull String startYmd, int repeatDays) {
        try {
            Date start = ISO.parse(startYmd); if (start==null) return "";
            Calendar c = Calendar.getInstance(); c.setTime(start);
            c.add(Calendar.DAY_OF_YEAR, repeatDays);
            return (c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.DAY_OF_MONTH);
        } catch (ParseException e) { return ""; }
    }
}