package com.shopgun.android.sdk.utils;

import com.shopgun.android.sdk.network.Request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class SgnUtils {

    public static final String TAG = SgnUtils.class.getSimpleName();

    /** UUID regex to do validation */
    public static final String UUID_REGEX = "([a-fA-F0-9]{8}-(?:[a-fA-F0-9]{4}-){3}[a-fA-F0-9]{12}){1}";
    private static final Pattern UUID_PATTERN = Pattern.compile(UUID_REGEX);
    /** Api v2 date format (RFC 822) */
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZ";
    /** String representation of epoc */
    private static final String DATE_EPOC = "1970-01-01T00:00:00+0000";
    /** Single instance of SimpleDateFormat to save time and memory */
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT, Locale.US);
    private static final Object DATE_LOCK = new Object();

    private SgnUtils() {
        // private
    }

    /**
     * Create universally unique identifier.
     * @return Universally unique identifier (UUID).
     */
    public static String createUUID() {
        return UUID.randomUUID().toString();
    }

    public static void isValidUuidOrThrow(String uuid) {
        if (!isValidUUID(uuid)) {
            throw new IllegalArgumentException("The given UUID isn't valid: " + uuid);
        }
    }

    public static boolean isValidUUID(String uuid) {
        return UUID_PATTERN.matcher(uuid).matches();
    }

    /**
     * Builds a url + query string.<br>
     * e.g.: https://api.etilbudsavis.dk/v2/catalogs?order_by=popular
     *
     * @param r to build from
     * @return A String
     */
    public static String requestToUrlAndQueryString(Request<?> r) {
        if (r == null || r.getUrl() == null) {
            return null;
        }
        if (r.getParameters() == null || r.getParameters().isEmpty()) {
            return r.getUrl();
        }
        return r.getUrl() + "?" + mapToQueryString(r.getParameters(), r.getParamsEncoding());
    }

    /**
     * Returns a string of parameters, ordered alfabetically (for better cache performance)
     *
     * @param apiParams to convert into query parameters
     * @param encoding encoding to use
     * @return a string of parameters
     */
    public static String mapToQueryString(Map<String, String> apiParams, String encoding) {
        StringBuilder sb = new StringBuilder();
        if (apiParams != null) {
            LinkedList<String> keys = new LinkedList<String>(apiParams.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                String value = valueIsNull(apiParams.get(key));
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(encode(key, encoding)).append("=").append(encode(value, encoding));
            }
        }
        return sb.toString();
    }

    /**
     * Method for handling null-values
     *
     * @param value to check
     * @return s string where the empty string "" represents null
     */
    private static String valueIsNull(Object value) {
        return value == null ? "" : value.toString();
    }

    /**
     * URL encoding of strings
     *
     * @param value    to encode
     * @param encoding encoding to use
     * @return an URL-encoded string
     */
    @SuppressWarnings("deprecation")
    public static String encode(String value, String encoding) {
        try {
            value = URLEncoder.encode(value, encoding);
        } catch (NullPointerException e) {
            // Happens on older devices (HTC Sense)?
            value = URLEncoder.encode(value);
        } catch (UnsupportedEncodingException e) {
            value = URLEncoder.encode(value);
        } catch (IllegalCharsetNameException e) {
            value = URLEncoder.encode(value);
        }
        return value;
    }

    /**
     * Convert an API date of the format "2013-03-03T13:37:00+0000" into a Date object.
     *
     * @param date to convert
     * @return a Date object
     */
    public static Date stringToDate(String date) {
        synchronized (DATE_LOCK) {
            try {
                return SIMPLE_DATE_FORMAT.parse(date);
            } catch (ParseException e) {
                return new Date(0);
            }
        }
    }

    /**
     * Convert an API date of the format "2013-03-03T13:37:00+0000" into a Date object.
     *
     * @param date to convert
     * @return a Date object
     */
    public static Date stringToDateThrows(String date) throws ParseException {
        synchronized (DATE_LOCK) {
            return SIMPLE_DATE_FORMAT.parse(date);
        }
    }

    /**
     * Convert a Date object into a date string, that will be accepted by the API.
     * <p>The format for an API date is {@link #DATE_FORMAT}</p>
     *
     * @param date to convert
     * @return a string
     */
    public static String dateToString(Date date) {
        synchronized (DATE_LOCK) {
            try {
                return SIMPLE_DATE_FORMAT.format(date);
            } catch (NullPointerException e) {
                return DATE_EPOC;
            }
        }
    }

    /**
     * Checks a given status code, is in the range from (including) 200 to (not including) 300, or 304
     *
     * @param statusCode to check
     * @return true is is success, else false
     */
    public static boolean isSuccess(int statusCode) {
        return 200 <= statusCode && statusCode < 300 || statusCode == 304;
    }

    /**
     * Copy all elements from an iterator to a {@link List}
     * @param it An {@link Iterator}
     * @param <T> Any type
     * @return A list containing all elements from the {@link Iterator}
     */
    public static <T> List<T> copyIterator(Iterator<T> it) {
        List<T> copy = new ArrayList<T>();
        while (it.hasNext()) {
            copy.add(it.next());
        }
        return copy;
    }

    /**
     * Converts 32 bits of the int into 4 bytes array (in big-endian order).
     * @param integer to be converted
     * @return the correspondent byte array
     */
    public static byte[] intToByteArray (final int integer) {
        byte[] result = new byte[4];

        result[0] = (byte)((integer & 0xFF000000) >> 24);
        result[1] = (byte)((integer & 0x00FF0000) >> 16);
        result[2] = (byte)((integer & 0x0000FF00) >> 8);
        result[3] = (byte)(integer & 0x000000FF);

        return result;
    }

}
