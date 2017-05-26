package com.tiamaes.cloud.logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;


/**
 * @author Chen
 */
abstract class AbstractAnnotationAdvice<T extends Annotation> {
	
	protected final Class<T> annotationClass;
	
    protected AbstractAnnotationAdvice(final Class<T> annotationClass) {
        this.annotationClass = annotationClass;
        
    }

    protected abstract Object execute(final ProceedingJoinPoint pjp) throws Throwable;
    
    
    public Method getMethodToExecute(final JoinPoint jp) throws NoSuchMethodException {
		final Signature sig = jp.getSignature();
        if (!(sig instanceof MethodSignature)) {
            throw new RuntimeException("This annotation is only valid on a method.");
        }
        final MethodSignature msig = (MethodSignature) sig;
        final Object target = jp.getTarget();
        
        String name = msig.getName();
        Class<?>[] parameters = msig.getParameterTypes();
        
        return target.getClass().getMethod(name, parameters);
	}
	/**
	 * @param annotation
	 * @param expectedAnnotationClass
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected Object _populate(final Annotation annotation,
			final Class<? extends Annotation> expectedAnnotationClass, String name)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		final Method namespaceMethod = expectedAnnotationClass.getDeclaredMethod(name, (Class<?>[]) null);
		return namespaceMethod.invoke(annotation, (Object[]) null);
	}
}
