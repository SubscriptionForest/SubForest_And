package com.example.subforest.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class DateUtils {
    private static final SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat MD  = new SimpleDateFormat("M/d", Locale.US); // 7/25 형태

    private DateUtils(){}

    public static String addDaysMd(String yyyyMmDd, int days) {
        Calendar c = toCal(yyyyMmDd);
        c.add(Calendar.DAY_OF_YEAR, days);
        return MD.format(c.getTime());
    }

    public static String toMd(String yyyyMmDd) {
        Calendar c = toCal(yyyyMmDd);
        return MD.format(c.getTime());
    }

    public static String periodLabel(String startYmd, int repeatDays) {
        // 예: "7/25 - 8/25"
        return toMd(startYmd) + " - " + addDaysMd(startYmd, repeatDays);
    }

    public static String nextPaymentOrEnd(String startYmd, int repeatDays) {
        // 다음결제일 혹은 종료예정일 = start + repeatDays
        return addDaysMd(startYmd, repeatDays);
    }

    private static Calendar toCal(String ymd) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(ISO.parse(ymd));
            return c;
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date: " + ymd, e);
        }
    }
}