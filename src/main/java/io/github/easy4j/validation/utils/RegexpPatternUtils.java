package io.github.easy4j.validation.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;


/**
 */
public class RegexpPatternUtils {

	protected static Logger LOG = LoggerFactory.getLogger(RegexpPatternUtils.class);
	protected static ConcurrentMap<String, Pattern> COMPLIED_PATTERN = new ConcurrentHashMap<String, Pattern>();

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
