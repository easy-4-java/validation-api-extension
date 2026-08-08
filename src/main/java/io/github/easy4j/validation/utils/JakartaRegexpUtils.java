package io.github.easy4j.validation.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Utility for JDK regular expression matching with a bounded pattern cache.
 *
 * <p>Compiled {@link Pattern} instances are cached in a bounded {@link ConcurrentHashMap}
 * managed by {@link RegexpPatternCache}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see RegexpPatternCache
 */
public class JakartaRegexpUtils {

	protected static ConcurrentMap<String, Pattern> COMPLIED_PATTERN = new ConcurrentHashMap<String, Pattern>();

	/**
	 * Tests whether the entire input string matches the given regex.
	 *
	 * @param regexp the regular expression
	 * @param str    the string to match
	 * @return {@code true} if the entire input matches
	 */
	public static boolean matches(String regexp, String str) {
        Pattern pattern = getPattern(regexp);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

	/**
	 * Returns a compiled {@link Pattern} for the given regex, using a bounded cache.
	 *
	 * @param regexp the regular expression (may be {@code null} or blank)
	 * @return the compiled pattern, or {@code null} if the input is blank
	 */
	public static Pattern getPattern(String regexp) {
		if (StringUtils.isNotBlank(regexp)) {
			Pattern ret = COMPLIED_PATTERN.get(regexp);
			if (ret != null) {
				return ret;
			}
			ret = Pattern.compile(regexp);
			return RegexpPatternCache.cache(COMPLIED_PATTERN, regexp, ret);
		}
		return null;
	}

}
