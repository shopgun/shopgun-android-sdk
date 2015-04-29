package com.eTilbudsavis.etasdk.utils;

import java.util.regex.Pattern;

import com.eTilbudsavis.etasdk.Constants;

public class Validator {
	
	public static final String TAG = Constants.getTag(Validator.class);
	
	public static final String APP_VERSION_FORMAT = "(\\d+)\\.(\\d+)\\.(\\d+)([+-][0-9A-Za-z-.]*)?";
	
	public static final String xAPP_VERSION_FORMAT = "(\\d+)\\.(\\d+)\\.(\\d+)([-]([0-9A-Za-z-.]+)*)?";
	
	//           \d+\.\d+\.\d+(\-[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?(\+[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?
	
	/**
	 * Checks if a given integer is a valid birth year.<br>
	 * Requirements: birth year is in the span 1900 - 2015.
	 * @param birthyear
	 * @return
	 */
	public static boolean isBirthyearValid(Integer birthyear) {
		return birthyear >= 1900 ? (birthyear <= 2015) : false ;
	}
	
	/**
	 * A very naive implementation of email validation.<br>
	 * Requirement: 
	 * <li>Email != null</li>
	 * <li>must contain a single '@' char</li>
	 * <li>At least one char before and after the '@'</li>
	 * <li>email.trim() must be equal to email</li>
	 * <br />
	 * But why not just use an email RegEx, like android.util.Patterns.EMAIL_ADDRESS?
	 * <br />
	 * Simple: it doesn't comply with RFC 2822. <br />
	 * Our {@link Validator} doesn't either. But it's better to let the API decide if it's valid. 
	 * @param email to check
	 * @return true if email is valid, else false
	 */
	public static boolean isEmailValid(String email) {
		
		if (email == null) {
			return false;
		}
		
		String[] split = email.split("@"); 
		if (split.length != 2) {
			return false;
		}
		if (split[0].length() == 0 || split[1].length() == 0) {
			return false;
		}
		
		String trim = email.trim();
		if (!trim.equals(email)) {
			return false;
		}
		
		return true;
	}

	/**
	 * Checks if a given string is a valid gender.<br>
	 * Requirements: String is either 'male' or 'female' (not case sensitive).
	 * @param birthyear
	 * @return
	 */
	public static boolean isGenderValid(String gender) {
		if (gender!=null) {
			String g = gender.toLowerCase().trim();
			return (g.equals("male") || g.equals("female") );
		}
		return false;
	}

	/**
	 * A simple regular expression to check if the app-version string can be accepted by the API
	 * @param version to check
	 * @return true, if the version matched the regex
	 */
	public static boolean isAppVersionValid(String version) {
		if (version == null) {
			return false;
		}
	    return Pattern.compile(APP_VERSION_FORMAT).matcher(version).matches();
	}
	
}