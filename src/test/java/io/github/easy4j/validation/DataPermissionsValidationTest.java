package io.github.easy4j.validation;

import io.github.easy4j.validation.constraints.RequiresDataPermissions;
import io.github.easy4j.validation.constraintvalidators.RequiresDataPermissionsValidator;
import io.github.easy4j.validation.provider.DataScopeProvider;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** 通过真实 Bean Validation 验证权限配置、约束元数据及请求隔离。 */
class DataPermissionsValidationTest {
    @Test
    void missingProviderRejectsNonNullValue() {
        try (ValidatorFactory factory = factory(null)) {
            assertThrows(ValidationException.class, () -> factory.getValidator().validate(new Request("any")));
        }
    }

    @Test
    void missingProviderIsConfigurationErrorEvenForNullValue() {
        try (ValidatorFactory factory = factory(null)) {
            assertThrows(ValidationException.class, () -> factory.getValidator().validate(new Request(null)));
        }
    }

    @Test
    void configuredProviderReceivesDataTypeAndRejectsUnauthorizedValue() {
        AtomicInteger calls = new AtomicInteger();
        try (ValidatorFactory factory = factory((type, value) -> {
            calls.incrementAndGet();
            return "order".equals(type) && "allowed".equals(value);
        })) {
            Validator validator = factory.getValidator();
            assertTrue(validator.validate(new Request("allowed")).isEmpty());
            assertEquals(1, validator.validate(new Request("denied")).size());
            assertEquals(1, validator.validate(new Request(null)).size());
            assertEquals(2, calls.get());
        }
    }

    @Test
    void groupsMessageAndPayloadRemainStandardConstraints() {
        try (ValidatorFactory factory = factory((type, value) -> false)) {
            Validator validator = factory.getValidator();
            assertTrue(validator.validate(new GroupedRequest()).isEmpty());
            Set<ConstraintViolation<GroupedRequest>> failures = validator.validate(new GroupedRequest(), Restricted.class);
            assertEquals(1, failures.size());
            ConstraintViolation<GroupedRequest> violation = failures.iterator().next();
            assertEquals("orderId", violation.getPropertyPath().toString());
            assertEquals("订单不可见", violation.getMessage());
            assertTrue(violation.getConstraintDescriptor().getPayload().contains(Audit.class));
        }
    }

    @Test
    void differentAnnotationsKeepTheirOwnDataType() {
        try (ValidatorFactory factory = factory((type, value) -> "tenant".equals(type))) {
            Set<ConstraintViolation<TwoDomains>> failures = factory.getValidator().validate(new TwoDomains());
            assertEquals(1, failures.size());
            assertEquals("project", failures.iterator().next().getPropertyPath().toString());
        }
    }

    @Test
    void providerFailureDoesNotBecomePermissionGranted() {
        IllegalStateException failure = new IllegalStateException("permission backend unavailable");
        try (ValidatorFactory factory = factory((type, value) -> { throw failure; })) {
            ValidationException thrown = assertThrows(ValidationException.class,
                    () -> factory.getValidator().validate(new Request("allowed")));
            assertSame(failure, thrown.getCause());
        }
    }

    @Test
    void separateFactoriesDoNotShareProviders() {
        try (ValidatorFactory first = factory((type, value) -> "first".equals(value));
             ValidatorFactory second = factory((type, value) -> "second".equals(value))) {
            assertTrue(first.getValidator().validate(new Request("first")).isEmpty());
            assertEquals(1, second.getValidator().validate(new Request("first")).size());
            assertTrue(second.getValidator().validate(new Request("second")).isEmpty());
            assertEquals(1, first.getValidator().validate(new Request("second")).size());
        }
    }

    @Test
    void sharedValidatorUsesCurrentRequestContextOnEveryCall() throws Exception {
        ThreadLocal<String> currentTenant = new ThreadLocal<>();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try (ValidatorFactory factory = factory((type, value) -> Objects.equals(currentTenant.get(), value))) {
            Validator validator = factory.getValidator();
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                final String tenant = "tenant-" + i;
                results.add(executor.submit(() -> {
                    currentTenant.set(tenant);
                    try {
                        return validator.validate(new Request(tenant)).isEmpty()
                                && validator.validate(new Request("other")).size() == 1;
                    } finally {
                        currentTenant.remove();
                    }
                }));
            }
            for (Future<Boolean> result : results) {
                assertTrue(result.get());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private ValidatorFactory factory(DataScopeProvider provider) {
        jakarta.validation.Configuration<?> configuration = Validation.byDefaultProvider().configure()
                .messageInterpolator(new ParameterMessageInterpolator());
        if (Objects.nonNull(provider)) {
            ConstraintValidatorFactory defaults = configuration.getDefaultConstraintValidatorFactory();
            configuration.constraintValidatorFactory(new ConstraintValidatorFactory() {
                @Override
                public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> type) {
                    if (type == RequiresDataPermissionsValidator.class) {
                        return type.cast(new RequiresDataPermissionsValidator(provider));
                    }
                    return defaults.getInstance(type);
                }

                @Override
                public void releaseInstance(ConstraintValidator<?, ?> instance) {
                    if (!(instance instanceof RequiresDataPermissionsValidator)) {
                        defaults.releaseInstance(instance);
                    }
                }
            });
        }
        return configuration.buildValidatorFactory();
    }

    private static class Request {
        @RequiresDataPermissions(dataType = "order")
        private final String orderId;

        Request(String orderId) {
            this.orderId = orderId;
        }
    }

    interface Restricted { }
    interface Audit extends Payload { }

    private static class GroupedRequest {
        @RequiresDataPermissions(dataType = "order", message = "订单不可见", groups = Restricted.class, payload = Audit.class)
        private final String orderId = "denied";
    }

    private static class TwoDomains {
        @RequiresDataPermissions(dataType = "tenant")
        private final String tenant = "1";
        @RequiresDataPermissions(dataType = "project")
        private final String project = "1";
    }
}
