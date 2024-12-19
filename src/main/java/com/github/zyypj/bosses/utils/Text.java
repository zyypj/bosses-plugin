package com.github.zyypj.bosses.utils;

import java.util.List;

public class Text {

    public static String colorTranslate(String string) {
        return string.replace("&", "§");
    }

    public static List<String> colorTranslate(List<String> stringList) {
        stringList.replaceAll(s -> s.replace("&", "ยง"));
        return stringList;
    }

    public static List<String> listReplace(List<String> list, String from, String to) {
        list.replaceAll(s -> s.replace(from, to));
        return list;
    }
}
