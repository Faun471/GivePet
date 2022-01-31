package me.faun.givepet.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {
    public static String unixToDate(long unix) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date(unix));
    }
}
