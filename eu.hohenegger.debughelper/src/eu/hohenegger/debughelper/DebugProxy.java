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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @Example Foo foo = (Foo) DebugProxy.newInstance(new FooImpl()); </br>
 *          foo.bar(null);
 * 
 * @See http://docs.oracle.com/javase/1.4.2/docs/guide/reflection/proxy.html
 * 
 * @author Max Hohenegger
 * 
 */
public class DebugProxy implements java.lang.reflect.InvocationHandler {

	private Object obj;

	public static Object newInstance(Object obj) {
		ClassLoader classLoader = obj.getClass().getClassLoader();
		return Proxy.newProxyInstance(classLoader, getInterfaces(obj.getClass()), new DebugProxy(obj));
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
		HashSet<Class<?>> hashSet = new HashSet<Class<?>>(interfaces);
		return hashSet.toArray(new Class<?>[hashSet.size()]);
	}

	private DebugProxy(Object obj) {
		this.obj = obj;
	}

	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
		Object result;
		try {
			StackTraceElement element = Thread.currentThread().getStackTrace()[3];
			System.out.print(MessageFormat.format("({0}:{1, number,#}) : ", element.getFileName(), element.getLineNumber()));
			System.out.print(m.getDeclaringClass().getName() + "." + m.getName() + "(");
			if (args != null) {
				for (Object object : args) {
					System.out.print(object + ", ");
				}
			}
			result = m.invoke(obj, args);
			System.out.println(") => " + result);
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