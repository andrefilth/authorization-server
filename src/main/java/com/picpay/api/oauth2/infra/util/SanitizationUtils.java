/*
 * @(#)SanitizationUtils.java 1.0 31/10/2020
 *
 * Copyright (c) 2020, PicPay S.A. All rights reserved.
 * PicPay S.A. proprietary/confidential. Use is subject to license terms.
 */

package com.picpay.api.oauth2.infra.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.stripAccents;

/**
 * Class comments go here...
 *
 * @author André Franco
 * @version 1.0 13/10/2020
 */
public class SanitizationUtils {

    private static final Pattern PHONE_REGEX = Pattern.compile("(\\d{2}?9?\\d{1,8})$");

    public static String sanitizeEmail(String email) {
        return removeSpaces(email)
            .toLowerCase();
    }

    private static String removeSpaces(final String str) {
        return str.replaceAll("\\s", "");
    }
    public static String sanitizeUsername(final String username) {
        final var stripAccents = stripAccents(username);
        final var removeSpaces = removeSpaces(stripAccents);
        final var removeStart = removeStart(removeSpaces, "@");
        return removeStart.toLowerCase();
    }

    public static String sanitizePhone(String phone) {
        final String  phoneTrims = "[ ()-]";
        final String phoneReplace = phone.replaceAll(phoneTrims, "");
        final Matcher matcher = PHONE_REGEX.matcher(phoneReplace);
        return matcher.find() ? matcher.group(1) : phone;
    }

}
