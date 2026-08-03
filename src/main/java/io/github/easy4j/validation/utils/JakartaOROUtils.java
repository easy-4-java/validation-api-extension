package io.github.easy4j.validation.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.oro.text.regex.*;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
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
	 * 正则表达式验证方法:匹配表达式则返回true,不匹配则返回false
	 * @param regexp
	 * @param mask
	 * @param input
	 * @return		：
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

	public static Pattern getPattern(String regexp, int mask) throws MalformedPatternException {
		if (StringUtils.isNotBlank(regexp)) {
			String cacheKey = regexp + '\u0000' + mask;
			Pattern ret = COMPLIED_PATTERN.get(cacheKey);
			if (ret != null) {
				return ret;
			}
			ret = compiler.compile(regexp,mask);
			Pattern existing = COMPLIED_PATTERN.putIfAbsent(cacheKey, ret);
			if (existing != null) {
				ret = existing;
			}
			return ret;
		}
		return null;
	}

	private static PatternMatcher matcher() {
		return MATCHER.get();
	}


}
