package io.github.easy4j.validation.provider;

/** 应用提供的数据权限决策；实现应从当前请求上下文读取身份，不缓存请求级授权结果。 */
@FunctionalInterface
public interface DataScopeProvider {
    /**
     * 判断当前调用方是否可以访问指定数据。
     *
     * @param dataType 注解声明的业务数据类型
     * @param data 被校验的非空值
     * @return 允许访问时返回 true
     */
    boolean hasPermissions(String dataType, Object data);
}
