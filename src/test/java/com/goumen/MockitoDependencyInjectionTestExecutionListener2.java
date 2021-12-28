//package com.goumen;
//
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.springframework.aop.support.AopUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
//import org.springframework.test.context.TestContext;
//import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
//
//import java.lang.annotation.Annotation;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.lang.reflect.Proxy;
//
//public class MockitoDependencyInjectionTestExecutionListener2 extends
//        DependencyInjectionTestExecutionListener {
//    @Override
//    protected void injectDependencies(TestContext testContext) throws Exception {
//        super.injectDependencies(testContext);
//        init(testContext);
//    }
//
//    private void init(final TestContext testContext) throws Exception {
//        Object bean = testContext.getTestInstance();
//        Field[] fields = bean.getClass().getDeclaredFields();
//        for (Field field : fields) {
//            Annotation[] annotations = field.getAnnotations();
//            for (Annotation annotation : annotations) {
//                if (annotation instanceof Mock) {
//                    //注入Mock实例
//                    MockObject obj = new MockObject();
//                    obj.setType(field.getType());
//                    obj.setObj(Mockito.mock(field.getType()));
//                    field.setAccessible(true);
//                    field.set(bean, obj.getObj());
//                    mockObjectMap.put(field.getName(), obj);
//                } else if (annotation instanceof Autowired){
//                    injectFields.add(field);
//                }
//            }
//        }
//        AutowireCapableBeanFactory factory = testContext.getApplicationContext().getAutowireCapableBeanFactory();
//        //Autowired注解注入mock对象
//        for (Field field : injectFields) {
//            field.setAccessible(true);
//            Object object = field.get(bean);
//            if (object instanceof Proxy){
//                Class targetClass = AopUtils.getTargetClass(object);
//                if (targetClass == null)
//                    return;
//                Field[] targetFields = targetClass.getDeclaredFields();
//                for (Field targetField : targetFields) {
//                    targetField.setAccessible(true);
//                    if (mockObjectMap.get(targetField.getName()) == null) {
//                        continue;
//                    }
//                    targetField.set(getTargetObject(object, mockObjectMap.get(targetField.getName()).getType()), mockObjectMap.get(targetField.getName()).getObj());
//                }
//            }else{
//                Object realObject = factory.getBean(field.getName());
//                if (null != realObject) {
//                    Method[] methods = realObject.getClass().getDeclaredMethods();
//                    for (Method method : methods) {
//                        if (method.getName().equalsIgnoreCase("set" + field.getName())) {
//                            method.invoke(realObject, mockObjectMap.get(field.getName()).getObj());
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
