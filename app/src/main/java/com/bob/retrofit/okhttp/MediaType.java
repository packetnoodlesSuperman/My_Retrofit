package com.bob.retrofit.okhttp;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MediaType {

    //可以使用这些字符
    private static final String TOKEN = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)";

    private static final Pattern TYPE_SUBTYPE = Pattern.compile(TOKEN + "/" + TOKEN);

    //比如application/x-www-form-urlencoded
    public static MediaType parse(String string) {
        Matcher typeSubtype = TYPE_SUBTYPE.matcher(string);
        if (!typeSubtype.lookingAt()) {
            return null;
        }
        //application
        String type = typeSubtype.group(1).toLowerCase(Locale.US);
        //x-www-form-urlencoded
        String subtype = typeSubtype.group(2).toLowerCase(Locale.US);

        String charset = null;
        Matcher parameter = PARAMETER.matcher(string);

        return new MediaType(string, type, subtype, charset);
    }

}