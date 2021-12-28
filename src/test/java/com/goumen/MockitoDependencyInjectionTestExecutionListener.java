package com.goumen;

import org.mockito.*;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 只对InjectMocks字段进行主动注入，不修改Autowired引入的对象，防止修改spring运行环境
 */
public class MockitoDependencyInjectionTestExecutionListener extends DependencyInjectionTestExecutionListener {
    private Set<Field> injectFields = new HashSet<>();
    private Map<String, Object> mockObjectMap = new HashMap<>();

    @Override
    protected void injectDependencies(TestContext testContext) throws Exception {
        super.injectDependencies(testContext);
        init(testContext);
    }

    /**
     * InjectMocks注解的字段，会自动注入mock、spy、autowired注解的字段；
     * 1、主动调用MockitoAnnotations.initMocks(bean)，完成mock信息的初始化；
     * 2、为InjectMocks字段注入依赖
     *
     * when A dependences on B
     * mock B or Spy on targetObject of bean get from Spring IoC Container whose type is B.class or beanName is BImpl
     *
     * @param testContext
     */
    private void init(TestContext testContext) throws Exception {
        AutowireCapableBeanFactory factory = testContext.getApplicationContext().getAutowireCapableBeanFactory();
        // 用例类 XxxTest.java
        Object bean = testContext.getTestInstance();
        MockitoAnnotations.initMocks(bean);
        Field[] fields = bean.getClass().getDeclaredFields();

        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof Mock) {
                    Class<?> clazz = field.getType();
                    Object object = Mockito.mock(clazz);
                    field.setAccessible(true);
                    field.set(bean, object);
                    mockObjectMap.put(field.getName(), object);
                } else if (annotation instanceof Spy) {
                    Object fb = factory.getBean(field.getName()); //may be a proxy that can not be spy because $Proxy is final
                    Object targetSource = AopTargetUtils.getTarget(fb);
                    Object spyObject = Mockito.spy(targetSource);
                    if (!fb.equals(targetSource)) { //proxy
                        if (AopUtils.isJdkDynamicProxy(fb)) {
                            setJdkDynamicProxyTargetObject(fb, spyObject);
                        } else { //cglib
                            setCglibProxyTargetObject(fb, spyObject);
                        }
                    } else {
                        mockObjectMap.put(field.getName(), spyObject);
                    }
                    field.setAccessible(true);
                    field.set(bean, spyObject);
                } else if (annotation instanceof Autowired) {
                    Object fb = factory.getBean(field.getName());
                    mockObjectMap.put(field.getName(), fb);
                } else if (annotation instanceof InjectMocks) {
                    injectFields.add(field);
                }
            }
        }

        // 处理InjectMock信息,注入mock,spy,autowired
        for (Field field : injectFields) {
            field.setAccessible(true);
            Object injectObject = field.get(bean);
            if (AopUtils.isAopProxy(injectObject)) {
                // 这个条件对于使用InjectMocks注解的字段，应该都是不满足的。
                Class targetClass = AopUtils.getTargetClass(injectObject);
                if (targetClass == null) {
                    return;
                }
                Object targetSource = AopTargetUtils.getTarget(injectObject);
                Field[] targetFields = targetClass.getDeclaredFields();
                for (Field targetField : targetFields) {
                    targetField.setAccessible(true);
                    if (mockObjectMap.get(targetField.getName()) != null) {
                        ReflectionTestUtils.setField(targetSource, targetField.getName(), mockObjectMap.get(targetField.getName()));
                    }
                }

            } else {
                Field[] foFields = injectObject.getClass().getDeclaredFields();
                for (Field foField : foFields) {
                    foField.setAccessible(true);
                    Object fieldObject = foField.get(injectObject);
                    if (fieldObject == null && mockObjectMap.containsKey(foField.getName())) {
                        ReflectionTestUtils.setField(injectObject, foField.getName(), mockObjectMap.get(foField.getName()));

                    }
                }
            }
        }
    }

    private void setCglibProxyTargetObject(Object proxy, Object spyObject) throws NoSuchFieldException, IllegalAccessException {
        Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        h.setAccessible(true);
        Object dynamicAdvisedInterceptor = h.get(proxy);
        Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).setTarget(spyObject);

    }

    private void setJdkDynamicProxyTargetObject(Object proxy, Object spyObject) throws NoSuchFieldException, IllegalAccessException {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy) h.get(proxy);
        Field advised = aopProxy.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        ((AdvisedSupport) advised.get(aopProxy)).setTarget(spyObject);

    }


}