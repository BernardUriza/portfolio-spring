package com.portfolio.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring Context holder for accessing Spring beans from non-Spring-managed classes
 * This is primarily used by JPA entity listeners that are instantiated by JPA, not Spring
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-27
 */
@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext context;

    /**
     * Get the Spring Application Context
     * @return ApplicationContext instance
     */
    public static ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * Get a Spring-managed bean by type
     * @param beanClass The class of the bean to retrieve
     * @param <T> The type of the bean
     * @return The bean instance
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    /**
     * Set the Application Context (called by Spring during initialization)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.context = applicationContext;
    }
}
