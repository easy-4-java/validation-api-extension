package io.github.easy4j.validation.constraintvalidators;

import io.github.easy4j.validation.constraints.RequiresDataPermissions;
import io.github.easy4j.validation.provider.DataScopeProvider;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;

import java.util.Objects;

/** 数据权限约束的唯一实现；Provider 必须由容器或 ConstraintValidatorFactory 提供。 */
public class RequiresDataPermissionsValidator implements ConstraintValidator<RequiresDataPermissions, Object> {
    @Inject
    private DataScopeProvider provider;
    private String dataType;

    /** 供标准 Bean Validation 工厂创建，由支持注入的容器设置 Provider。 */
    public RequiresDataPermissionsValidator() {
    }

    /**
     * 供自定义 ConstraintValidatorFactory 显式装配权限策略。
     * @param provider 必须配置的数据权限策略
     */
    public RequiresDataPermissionsValidator(DataScopeProvider provider) {
        this.provider = Objects.requireNonNull(provider, "DataScopeProvider must not be null");
    }

    @Override
    public void initialize(RequiresDataPermissions annotation) {
        this.dataType = annotation.dataType();
    }

    @Override
    public boolean isValid(Object data, ConstraintValidatorContext context) {
        if (Objects.isNull(provider)) {
            throw new ValidationException("DataScopeProvider is required for @RequiresDataPermissions; "
                    + "configure injection or a ConstraintValidatorFactory");
        }
        return Objects.nonNull(data) && provider.hasPermissions(dataType, data);
    }
}
