package io.github.easy4j.validation.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.oro.text.regex.*;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe utility for Perl 5 regular expression matching, containment checks,
 * and replacement using the Apache ORO library.
 *
 * <p>Compiled patterns are cached in a bounded {@link ConcurrentHashMap} managed by
 * {@link RegexpPatternCache}.  Each thread uses its own {@link Perl5Matcher} instance
 * via a {@link ThreadLocal}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see RegexpPatternCache
 */
@Slf4j
public class JakartaOROUtils {

	protected static ConcurrentMap<String, Pattern> COMPLIED_PATTERN = new ConcurrentHashMap<String, Pattern>();
	// 用于定义正规表达式对象模板类型
	protected static final PatternCompiler compiler = new Perl5Compiler();
	// Perl5Matcher 不是线程安全对象，每个线程使用独立实例。
	private static final ThreadLocal<PatternMatcher> MATCHER = new ThreadLocal<PatternMatcher>() {
		@Override
		protected PatternMatcher initialValue() {
			return new Perl5Matcher();
		}
	};

	/**
	 * Tests whether the entire input string matches the given Perl 5 regex.
	 *
	 * @param regexp the regular expression
	 * @param mask   the compilation mask flags
	 * @param input  the string to match
	 * @return {@code true} if the entire input matches
	 */
	public static boolean matches(String regexp, int mask , String input) {
		try {
			// 正规表达式模板
			Pattern hardPattern = getPattern(regexp, mask);
			// 返回匹配结果
			return matcher().matches(input, hardPattern);
		} catch (MalformedPatternException e) {
			log.error("matches error: {}", e.getMessage());
		}
		return false;

    }

	public static boolean matches(String regexp, int mask, PatternMatcherInput input) {
		try {
			Pattern pattern = getPattern(regexp , mask);
			return matcher().matches(input, pattern);
		} catch (MalformedPatternException e) {
			log.error("matches error: {}", e.getMessage());
		}
		return false;
	}


	public static boolean matchesPrefix(String regexp, int mask , String input) {
		try {
			// 正规表达式模板
			Pattern hardPattern = getPattern(regexp,mask);
			// 返回匹配结果
			return matcher().matchesPrefix(input, hardPattern);
		} catch (MalformedPatternException e) {
			log.error("matches error: {}", e.getMessage());
		}
		return false;

    }

	public static boolean matchesPrefix(String regexp, int mask, PatternMatcherInput input) {
		try {
			// 正规表达式模板
			Pattern hardPattern = JakartaOROUtils.getPattern(regexp,mask);
			// 返回匹配结果
			return matcher().matchesPrefix(input, hardPattern);
		} catch (MalformedPatternException e) {
			log.error("matches error: {}", e.getMessage());
		}
		return false;
	}

	/**
	 * Tests whether the input string contains a substring matching the given Perl 5 regex.
	 *
	 * @param regexp the regular expression
	 * @param mask   the compilation mask flags
	 * @param input  the string to search
	 * @return {@code true} if the input contains a match
	 */
	public static boolean contains(String regexp, int mask , String input) {
		try {
			//实例大小大小写敏感的正规表达式模板
			Pattern hardPattern = JakartaOROUtils.getPattern(regexp, mask);
			// 返回匹配结果;只匹配一次
			return matcher().contains(input, hardPattern);
		} catch (MalformedPatternException e) {
			log.error("matches error: {}", e.getMessage());
		}
		return false;

    }

	public static boolean contains(String regexp, int mask, PatternMatcherInput input) {
		try {
			// 正规表达式模板
			Pattern hardPattern = JakartaOROUtils.getPattern(regexp,mask);
			// 返回匹配结果
			boolean math = false;
			while (matcher().contains(input, hardPattern)) {
				math = true;
			}
			return math;
		} catch (MalformedPatternException e) {
			log.error("matches error: {}", e.getMessage());
		}
		return false;
	}

	public static MatchResult getMatchResult(String regexp, int mask , String input) {
		MatchResult matchResult = null;
		try {
			if(StringUtils.isNotBlank(regexp)){
				// 正规表达式模板
				Pattern hardPattern = JakartaOROUtils.getPattern(regexp,mask);
				// 返回匹配结果
				if (matcher().contains(input, hardPattern)) {
					matchResult = matcher().getMatch();
				}
			}
		} catch (MalformedPatternException e) {
			log.error("matches error: {}", e.getMessage());
		}
		return matchResult;
    }

	public static MatchResult getMatchResult(String regexp, int mask, PatternMatcherInput input) {
		MatchResult matchResult = null;
		try {
			if(StringUtils.isNotBlank(regexp)){
				// 正规表达式模板
				Pattern hardPattern = JakartaOROUtils.getPattern(regexp,mask);
				// 返回匹配结果
				while (matcher().contains(input, hardPattern)) {
					matchResult = matcher().getMatch();

				 /*
	             System.out.println(matchResult.begin(0));  // 0分组索引 , 匹配串开始值 ,如匹配串xxxx xxx, 总是0.
	             System.out.println(matchResult.end(0));    // 0分组索引, 匹配串结束值 , 如xxxxxxx , 则相应值为 4 3.
	             System.out.println(matchResult.beginOffset(0)); // 0分组索引,匹配串在源串开始索引
	             System.out.println(matchResult.endOffset(0));  // 0分组索引,匹配串在源串结束索引
	             System.out.println(matchResult.groups()); // 分组数量
	             System.out.println(matchResult.length()); // 匹配串长度
	             System.out.println(matchResult.toString()); // 匹配串
	             */

				}
			}
		} catch (MalformedPatternException e) {
			log.error("matches error: {}", e.getMessage());
		}
		return matchResult;
	}

	public static String replaces(String regexp, int mask , String input) {
		String output = input;
		try {
			if(StringUtils.isNotBlank(regexp)){
				// 正规表达式模板
				Pattern hardPattern = JakartaOROUtils.getPattern(regexp,mask);
				// 创建替换对象 Substiution
		        Perl5Substitution substiution = new Perl5Substitution(input);
		        // 文本替换
		        output = Util.substitute(matcher(), hardPattern, substiution, input, Util.SUBSTITUTE_ALL);
			}
		} catch (MalformedPatternException e) {
			log.error("matches error: {}", e.getMessage());
		}
        return output;
    }

	public static String replaces(String regexp, int mask, PatternMatcherInput input) {
		String output = input.toString();
		try {
			if(StringUtils.isNotBlank(regexp)){
				// 实例大小大小写敏感的正规表达式模板
				Pattern hardPattern = JakartaOROUtils.getPattern(regexp,mask);
				// 创建替换对象 Substiution
		        Perl5Substitution substiution = new Perl5Substitution(input.toString());
		        // 文本替换
		        output = Util.substitute(matcher(), hardPattern, substiution, input.toString(), Util.SUBSTITUTE_ALL);
			}
		} catch (MalformedPatternException e) {
			log.error("matches error: {}", e.getMessage());
		}
        return output;
	}

	/**
	 * Returns a compiled Perl 5 {@link Pattern} for the given regex and mask, using a
	 * bounded cache.
	 *
	 * @param regexp the regular expression
	 * @param mask   the compilation mask flags
	 * @return the compiled pattern, or {@code null} if the regexp is blank
	 * @throws MalformedPatternException if the regex is invalid
	 */
	public static Pattern getPattern(String regexp, int mask) throws MalformedPatternException {
		if (StringUtils.isNotBlank(regexp)) {
			String cacheKey = regexp + '\u0000' + mask;
			Pattern ret = COMPLIED_PATTERN.get(cacheKey);
			if (ret != null) {
				return ret;
			}
			ret = compiler.compile(regexp,mask);
			return RegexpPatternCache.cache(COMPLIED_PATTERN, cacheKey, ret);
		}
		return null;
	}

	private static PatternMatcher matcher() {
		return MATCHER.get();
	}


}
