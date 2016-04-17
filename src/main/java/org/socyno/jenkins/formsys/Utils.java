package org.socyno.jenkins.formsys;

import java.util.Map;
import java.util.List;
import java.util.Date;
import java.util.Arrays;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import jenkins.model.Jenkins;

import javax.annotation.CheckForNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;



import org.codehaus.groovy.control.CompilerConfiguration;

import org.socyno.jenkins.formsys.Messages;
import com.thoughtworks.xstream.core.util.Base64Encoder;


public final class Utils {

	private static final Logger logger = Logger.getAnonymousLogger();
    /**
     * Utility constructor.
     */
    private Utils() {
    }

    /**
     * Check if an string is null or empty.
     *
     * @param string
     * @return true if so.
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
    
    /**
     * Check if an string is null or blank.
     *
     * @param string
     * @return true if so.
     */
    public static boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
    
    
    /**
     * Null as empty.
     *
     */
    public static String nullAsEmpty(String s) {
        return s == null ? "" : s;
    }

    /**
     * Null as empty.
     *
     */
    public static String nullAsEmpty(Object s) {
        return s == null ? "" : s.toString();
    }
    
    public static boolean nullOrEmpty(String s) {
    	if ( s == null || s.isEmpty() ) {
    		return true;
    	}
    	return false;
    }
    
    public static String nullOrEmpty(String s, String x) {
    	if ( s == null || s.isEmpty() ) {
    		return x;
    	}
    	return s;
    }
    
    public static boolean nullOrBlank(String s) {
    	if ( s == null || s.trim().isEmpty() ) {
    		return true;
    	}
    	return false;
    }
    
    public static String nullOrBlank(String s, String x) {
    	if ( s == null || s.trim().isEmpty() ) {
    		return x;
    	}
    	return s;
    }
    
    /**
     * Trim a string, null to empty.
     *
     * @param string
     * @return trimed string
     */
    public static String trimOrEmpty(String s) {
        return s == null ? "" :  s.trim();
    }
    
    /**
     * long[] to String[]
     * 
     */
    public static String[] longArrToStringArr(long... ids) {
    	if ( ids == null ) return null;
    	String[] s = new String[ids.length];
    	for ( int i = 0; i < ids.length; i++ ) {
    		s[i] = "" + ids[i];
    	}
    	return s;
    }
    
    public static Long longGetMax(long... vs) {
    	if ( vs == null || vs.length == 0 ) {
    		return null;
    	}
    	if ( vs.length == 1 ) {
    		return vs[0];
    	}
    	Arrays.sort(vs);
    	return vs[vs.length - 1];
    }
    
    public static Long longGetMin(long... vs) {
    	if ( vs == null || vs.length == 0 ) {
    		return null;
    	}
    	if ( vs.length == 1 ) {
    		return vs[0];
    	}
    	Arrays.sort(vs);
    	return vs[0];
    }
    

    public static String[] stringSortedUnique(String... array) {
    	return stringSortedUnique(false, array);
    }
    
    public static String[] stringSortedUnique(boolean throwBlank, String... array) {
    	if ( array == null || array.length == 0 ) {
    		return new String[0];
    	}
    	if ( array.length == 1 ) {
    		if ( throwBlank ) {
    			return isNullOrBlank(array[0])
    				? new String[0] : array;
    		}
    		return array;
    	}
        List<String> list =
          	new ArrayList<String>();
        Arrays.sort(array);
        int index = 0;
        if ( throwBlank ) {
            while (isNullOrBlank(array[index])) {
            	if ( ++index == array.length ) {
            		return new String[0];
            	}
            }
            if ( index + 1 == array.length ) {
            	return new String[] {array[index]};
            }
        }
        list.add(array[index]);
        for ( int i = index + 1; i < array.length; i++ ) {
            if( array[i] != array[i-1] ) {
                list.add(array[i]);
            }
        }
        return list.toArray(new String[list.size()]);
    }
    
    public static String[] stringSortedUniqueThrowBlank(String... array) {
    	return stringSortedUnique(true, array);
    }
    
    /**
     * String[] to long[]
     * 
     */
    public static long[] stringArrToLongArr(String... numbers) {
    	if ( numbers == null ) return null;
    	long[] s = new long[numbers.length];
    	for ( int i = 0; i < numbers.length; i++ ) {
    		s[i] = parseLong(numbers[i]);
    	}
    	return s;
    }
    
    /**
     * int[] to String[]
     * 
     */
    public static String[] intArrToStringArr(int... ids) {
    	if (ids == null) return null;
    	String[] s = new String[ids.length];
    	for ( int i = 0; i < ids.length; i++ ) {
    		s[i] = "" + ids[i];
    	}
    	return s;
    }

	public static String encodeToBase64(@CheckForNull String str, String charset)
			throws UnsupportedEncodingException {
		return new Base64Encoder().encode(str.getBytes(charset));
	}

	public static String encodeToBase64(@CheckForNull String str) {
		return new Base64Encoder().encode(str.getBytes());
	}

	public static String decodeToBase64(@CheckForNull String str, String charset)
			throws UnsupportedEncodingException {
		return new String(new Base64Encoder().decode(str), charset);
	}

	public static String decodeToBase64(@CheckForNull String str) {
		return new String(new Base64Encoder().decode(str));
	}
	
    /**
     * String[] to int[]
     * 
     */
    public static int[] stringArrToIntArr(String... numbers) {
    	if ( numbers == null ) return null;
    	int[] s = new int[numbers.length];
    	for ( int i = 0; i < numbers.length; i++ ) {
    		s[i] = parseInt(numbers[i]);
    	}
    	return s;
    }
    
    /**
     * join long[] as a string
     * 
     */
    public static String longArrJoin(String sep, long... ids) {
    	String s = "";
    	sep = Utils.nullAsEmpty(sep);
    	if ( ids == null ) return null;
    	for ( int i = 0; i < ids.length; i++ ) {
    		s += (i == 0 ? "" : sep) + ids[i];
    	}
    	return s;
    }
    
    /**
     * join String[] to a string
     * 
     */
    public static String stringArrJoin(String sep, String... ids) {
    	String s = "";
    	if ( ids == null ) return null;
    	sep = Utils.nullAsEmpty(sep);
    	for ( int i = 0; i < ids.length; i++ ) {
    		s += (i == 0 ? "" : sep) + Utils.nullAsEmpty(ids[i]);
    	}
    	return s;
    }

    public static String stringJoin(String sep, Integer... arr) {
    	return stringJoin(sep, (Object[])arr);
    }
    
    public static String stringJoin(String sep, Long... arr) {
    	return stringJoin(sep, (Object[])arr);
    }
    
    public static String stringJoin(String sep, Object... arr) {
    	String s = "";
    	if ( arr == null ) return null;
    	sep = Utils.nullAsEmpty(sep);
    	for ( int i = 0; i < arr.length; i++ ) {
    		s += (i == 0 ? "" : sep) + Utils.nullAsEmpty(arr[i]);
    	}
    	return s;
    }

    public static String stringJoin(String sep, String... arr) {
    	return stringJoin(sep, (Object[])arr);
    }
    
    public static String stringToMD5(String str)
    			throws SysException {  
        MessageDigest md5 = null;
        try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new SysException(
				Messages.SCMErrorMissingMD5Algorithm(),
				e
			);
		}
        char[] charArray = str.toCharArray();  
        byte[] byteArray = new byte[charArray.length];  
  
        for (int i = 0; i < charArray.length; i++)  
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);  
        StringBuffer hexValue = new StringBuffer();  
        for (int i = 0; i < md5Bytes.length; i++){  
            int val;  
            if ((val = ((int) md5Bytes[i]) & 0xff) < 16) { 
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));  
        }  
        return hexValue.toString();
    }  
    
    /**
     * Split string by given separator, and throw empty
     * 
     */
    public static String[] splitThrowEmpty(String regex, String str) {
    	return splitThrowEmpty(regex, str, false);
    }
    
    public static String[] splitThrowEmpty(String regex, String str, boolean trimed) {
    	str = Utils.nullAsEmpty(str);
    	regex = Utils.nullAsEmpty(regex);
    	List<String> as = new ArrayList<String>();
    	for ( String s : str.split(regex) ) {
    		if ( !s.isEmpty() ) {
    			as.add(trimed ? s.trim() : s);
    		}
    	}
    	return as.toArray(new String[as.size()]);
    }


    /**
     *  Parser date from a given string
     * 
     */
    public static Date parseDateTime(String date) {
    	if ( (date = Utils.trimOrEmpty(date)).isEmpty() ) {
    		return null;
    	}
    	Matcher m;
    	if ( !(m = Pattern.compile(	
    	    	"^(\\d{4})/(\\d{1,2})/(\\d{1,2})"
    	   + "(?:\\s+(\\d{1,2})(?:\\:(\\d{1,2}))?(?:\\:(\\d{1,2}))?)?$"
    	).matcher((String)date)).find() ) {
    		return null;
    	}
		int year = Utils.parseInt(m.group(1));
		int month = Utils.parseInt(m.group(2));
		int day = Utils.parseInt(m.group(3));
		int hour = Utils.parseInt(m.group(4), 0);
		int minute =  Utils.parseInt(m.group(5), 0);
		int second =  Utils.parseInt(m.group(6), 0);
		if ( day < 1
		  || hour < 0 || hour > 23
		  || month < 1 || month > 12
		  || minute < 0 || minute > 59
		  || second < 0 || second > 59
		) {
			return null;
		}
		Calendar calendar;
		(calendar = Calendar.getInstance()).set(
			year, month - 1, 1, 0, 0, 0
		);
		calendar.roll(Calendar.DATE, -1);
		if ( day > calendar.get(Calendar.DATE) ) {
			return null;
		}
		calendar.set(year, month - 1, day, hour, minute, second);
		return calendar.getTime();
    }

    /**
     *  Parser integer from a given string
     * 
     */
    public static int parseInt(String s) {
    	return Integer.parseInt(trimOrEmpty(s), 10);
    }
    
    public static int parseInt(String s, int i) {
    	try {
    		return Integer.parseInt(trimOrEmpty(s), 10);
    	} catch ( Exception e ) {
    		return i;
    	}
    }
    
    /**
     *  Parser long from a given string
     * 
     */
    public static long parseLong(String s) throws NumberFormatException {
    	return Long.parseLong(s, 10);
    }
    
    public static long parseLong(String s, long l) {
    	try {
    		return Long.parseLong(s, 10);
    	} catch ( NumberFormatException e ) {
    		return l;
    	}
    }

	public static Long parseLongObj(String str) throws NumberFormatException {
		str = Utils.trimOrEmpty(str);
		if ( str.isEmpty() ) {
			return null;
		}
		return Long.parseLong(str, 10);
	}
	public static Long parseLongObj(String str, Long defaulValue) {
		if ( (str = Utils.trimOrEmpty(str)).isEmpty() ) {
			return null;
		}
		try {
    		return Long.parseLong(str, 10);
    	} catch ( NumberFormatException e ) {
    		return defaulValue;
    	}
	}
	
	public static Object evalGroovy(String script) throws RuntimeException {
        return evalGroovy(script, null);
    }
	
    public static Object evalGroovy(String script, Map<String, Object> variables) throws RuntimeException {
    	ClassLoader cl = null;
        try {
            cl = Jenkins.getInstance().getPluginManager().uberClassLoader;
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
        }
    	Binding context = new Binding();
    	if ( variables != null ) {
    		for( Map.Entry<String, Object> e : variables.entrySet() )
    		context.setVariable(e.getKey(), e.getValue());
    	}
    	return new GroovyShell(cl, context, CompilerConfiguration.DEFAULT)
    				.evaluate(script);
    }
    
    public static String regexpEscape(@CheckForNull String str) {
    	 return str.replaceAll("([^a-zA-z0-9])", "\\\\$1");
    }
}
