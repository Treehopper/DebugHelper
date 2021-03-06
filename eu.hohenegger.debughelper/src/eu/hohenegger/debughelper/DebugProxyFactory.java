/*******************************************************************************
 * Copyright (c) 2012 Max Hohenegger.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which 
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Max Hohenegger - initial implementation
 ******************************************************************************/
package eu.hohenegger.debughelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Example Foo foo = (Foo) DebugProxyFactory.INSTANCE.skipDeclaringClasses(Object.class).create(new FooImpl()); </br> foo.bar(null);
 * @See http://docs.oracle.com/javase/1.4.2/docs/guide/reflection/proxy.html
 */
public class DebugProxyFactory {

    public static final DebugProxyFactory INSTANCE = new DebugProxyFactory();
    private List<Class<?>> declaringInterfacesToBeSkipped;

    public class DebugProxy implements InvocationHandler {
        private List<Class<?>> declaringInterfacesToBeSkipped;
        private Object obj;

        public DebugProxy(List<Class<?>> declaringInterfacesToBeSkipped, Object obj2) {
            this.obj = obj2;
            this.declaringInterfacesToBeSkipped = declaringInterfacesToBeSkipped;
        }

        public Object invoke(Object proxy, Method methods, Object[] arguments) throws Throwable {
            StackTraceElement callers = Thread.currentThread().getStackTrace()[3];
            Class<?> declaringClass = methods.getDeclaringClass();
            String callerLog = MessageFormat.format("({0}:{1, number,#}): ", callers.getFileName(), callers.getLineNumber());

            StringBuffer argsLog = new StringBuffer();
            if (arguments != null) {
                for (Object object : arguments) {
                    argsLog.append(object + ", ");
                }
            }

            Object result = doInvoke(methods, arguments);

            if (declaringInterfacesToBeSkipped == null || !this.declaringInterfacesToBeSkipped.contains(declaringClass)) {
                System.out.println(callerLog + declaringClass.getName() + "." + methods.getName() + "(" + argsLog + ") => " + result);
            }

//            if (result instanceof Serializable) {
//                try {
//                    FileOutputStream fout = new FileOutputStream(System.getProperty("user.home") + "\\Desktop\\result.dat");
//                    ObjectOutputStream oos = new ObjectOutputStream(fout);
//                    oos.writeObject(result);
//                    oos.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                // TODO: de-serialize 
////                try {
////                    FileInputStream fin = new FileInputStream("thequeue.dat");
////                    ObjectInputStream ois = new ObjectInputStream(fin);
////                    theQueue = (Queue) ois.readObject();
////                    ois.close();
////                } catch (Exception e) {
////                    e.printStackTrace();
////                }
//            }

            return result;
        }

        private Object doInvoke(Method m, Object[] args) throws Throwable {
            Object result;
            try {
                result = m.invoke(obj, args);
                // System.out.println(") => " + result);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            } catch (Exception e) {
                throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
            } finally {
                // do nothing
            }
            return result;
        }

    }

    private DebugProxyFactory() {
        // do nothing
    }

    public Object create(Object obj) {
        ClassLoader classLoader = obj.getClass().getClassLoader();
        return Proxy.newProxyInstance(classLoader, getInterfaces(obj.getClass()), new DebugProxy(declaringInterfacesToBeSkipped, obj));
    }
    
	@SuppressWarnings("unchecked")
	public <T> T create(Class<T> clazz) {
    	T result = null;
        try {
            T newInstance = clazz.newInstance();
            result = (T) create(newInstance);
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return result;
    }

	public Object create(String className) {
    	Object result = null;
        try {
            Class<?> clazz = (Class<?>) Class.forName(className);
            result = create(clazz);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return result;
    }
    
    public DebugProxyFactory skipDeclaringClasses(Class<?>... interfaces) {
        this.declaringInterfacesToBeSkipped = Arrays.asList(interfaces);
        return this;
    }

    private static Class<?>[] getInterfaces(Class<? extends Object> clazz) {
        final List<Class<?>> interfaces = new ArrayList<Class<?>>();
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            if (currentClass.equals(Object.class)) {
                currentClass = null;
            } else {
                for (final Class<?> currInterface : currentClass.getInterfaces()) {
                    interfaces.add(currInterface);
                }
                currentClass = currentClass.getSuperclass();
            }
        }
        Set<Class<?>> hashSet = new HashSet<Class<?>>(interfaces);
        return hashSet.toArray(new Class<?>[hashSet.size()]);
    }

}