package org.datamigration.spring.method;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

import java.util.function.Supplier;

/**
 * Custom Data Migration {@link DefaultMethodSecurityExpressionHandler}.
 */
public class DataMigrationMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    @Override
    public EvaluationContext createEvaluationContext(Supplier<Authentication> authentication, MethodInvocation mi) {
        final StandardEvaluationContext context = (StandardEvaluationContext) super.createEvaluationContext(authentication, mi);
        final DataMigrationMethodSecurityExpressionRoot root = new DataMigrationMethodSecurityExpressionRoot(authentication);
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setTrustResolver(new AuthenticationTrustResolverImpl());
        root.setRoleHierarchy(getRoleHierarchy());
        context.setRootObject(root);
        return context;
    }
}