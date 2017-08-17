package me.akulakovsky.ffsearch.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static String toString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(date);
    }

    public static Date toDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toPrettyDate(Date date) {
        return new SimpleDateFormat("dd.MM.yyyy").format(date);
    }

    public static String toPrettyTime(Date date) {
        return new SimpleDateFormat("kk:mm").format(date);
    }
}
