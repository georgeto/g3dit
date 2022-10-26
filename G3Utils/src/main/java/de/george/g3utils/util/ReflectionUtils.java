package de.george.g3utils.util;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Mostly taken from org.reflections.reflections.
 * <p>
 * convenient java reflection helper methods
 * <p>
 * 1. some helper methods to get all types/methods/fields/constructors/properties matching some
 * predicates, generally:
 *
 * <pre>
 *  Set&#60?> result = getAllXXX(type/s, withYYY)
 * </pre>
 * <p>
 * where get methods are:
 * <ul>
 * <li>{@link #getAllSuperTypes(Class, com.google.common.base.Predicate...)}
 * <li>{@link #getAllFields(Class, com.google.common.base.Predicate...)}
 * <li>{@link #getAllMethods(Class, com.google.common.base.Predicate...)}
 * <li>{@link #getAllConstructors(Class, com.google.common.base.Predicate...)}
 * </ul>
 * <p>
 * and predicates included here all starts with "with", such as
 * <ul>
 * <li>{@link #withModifier(int)}
 * <li>{@link #withName(String)}
 * <li>{@link #withParameters(Class[])}
 * <li>{@link #withParametersAssignableTo(Class[])}
 * <li>{@link #withParametersAssignableFrom(Class[])}
 * <li>{@link #withPrefix(String)}
 * <li>{@link #withReturnType(Class)}
 * <li>{@link #withType(Class)}
 * <li>{@link #withTypeAssignableTo}
 * </ul>
 * <p>
 * <br>
 * for example, getting all getters would be:
 *
 * <pre>
 *      Set&#60Method> getters = getAllMethods(someClasses,
 *              Predicates.and(
 *                      withModifier(Modifier.PUBLIC),
 *                      withPrefix("get"),
 *                      withParametersCount(0)));
 * </pre>
 */
public abstract class ReflectionUtils {
	public static void setAccessible(AccessibleObject accessibleObject) {
		accessibleObject.setAccessible(true);
	}

	public static Class<?> getClassForName(String name) {
		try {
			return Class.forName(name);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor(parameterTypes);
			constructor.setAccessible(true);
			return constructor;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		try {
			Method method = clazz.getDeclaredMethod(name, parameterTypes);
			method.setAccessible(true);
			return method;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T toFunctionalInterface(Class<T> functionalInterface, Method method) {
		MethodHandles.Lookup caller = MethodHandles.lookup();

		var interfaceMethods = ReflectionUtils.getMethods(functionalInterface, m -> Modifier.isAbstract(m.getModifiers()));
		if (interfaceMethods.size() != 1)
			throw new IllegalArgumentException(
					"Expected exactly one abstract method in functional interface, found " + interfaceMethods.size());
		var interfaceMethod = interfaceMethods.iterator().next();

		MethodType interfaceMethodType = MethodType.methodType(interfaceMethod.getReturnType(), interfaceMethod.getParameterTypes());
		MethodType methodType = MethodType.methodType(method.getReturnType(), method.getDeclaringClass(), method.getParameterTypes());
		try {
			CallSite site = LambdaMetafactory.metafactory(caller, interfaceMethod.getName(), MethodType.methodType(functionalInterface),
					interfaceMethodType, caller.unreflect(method), methodType);
			return (T) site.getTarget().invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static Field getField(Class<?> clazz, String name) {
		try {
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public static Object getStaticFieldValue(Field field) {
		return getFieldValue(field, null);
	}

	public static <T> T getFieldValue(Field field, Object instance) {
		try {
			return (T) field.get(instance);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setFieldValue(Field field, Object instance, Object value) {
		try {
			field.set(instance, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setFinalStatic(Field field, Object newValue) {
		field.setAccessible(true);

		try {
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

			field.set(null, newValue);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T newInstance(Constructor<T> constructor, Object... args) {
		try {
			return constructor.newInstance(args);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T invoke(Method method, Object obj, Object... args) {
		try {
			return (T) method.invoke(obj, args);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * get all super types of given {@code type}, including, optionally filtered by
	 * {@code predicates}
	 * <p>
	 * include {@code Object.class} if {@link #includeObject} is true
	 */
	@SafeVarargs
	public static Set<Class<?>> getAllSuperTypes(final Class<?> type, Predicate<? super Class<?>>... predicates) {
		Set<Class<?>> result = Sets.newLinkedHashSet();
		if (type != null && !type.equals(Object.class)) {
			result.add(type);
			for (Class<?> supertype : getSuperTypes(type)) {
				result.addAll(getAllSuperTypes(supertype));
			}
		}
		return filter(result, predicates);
	}

	/** get the immediate supertype and interfaces of the given {@code type} */
	public static Set<Class<?>> getSuperTypes(Class<?> type) {
		Set<Class<?>> result = new LinkedHashSet<>();
		Class<?> superclass = type.getSuperclass();
		Class<?>[] interfaces = type.getInterfaces();
		if (superclass != null && !superclass.equals(Object.class)) {
			result.add(superclass);
		}
		if (interfaces != null && interfaces.length > 0) {
			result.addAll(Arrays.asList(interfaces));
		}
		return result;
	}

	/**
	 * get all methods of given {@code type}, up the super class hierarchy, optionally filtered by
	 * {@code predicates}
	 */
	@SafeVarargs
	public static Set<Method> getAllMethods(final Class<?> type, Predicate<? super Method>... predicates) {
		Set<Method> result = Sets.newHashSet();
		for (Class<?> t : getAllSuperTypes(type)) {
			result.addAll(getMethods(t, predicates));
		}
		return result;
	}

	/** get methods of given {@code type}, optionally filtered by {@code predicates} */
	@SafeVarargs
	public static Set<Method> getMethods(Class<?> t, Predicate<? super Method>... predicates) {
		return filter(t.isInterface() ? t.getMethods() : t.getDeclaredMethods(), predicates);
	}

	/**
	 * get all constructors of given {@code type}, up the super class hierarchy, optionally filtered
	 * by {@code predicates}
	 */
	@SafeVarargs
	public static Set<Constructor<?>> getAllConstructors(final Class<?> type, Predicate<? super Constructor<?>>... predicates) {
		Set<Constructor<?>> result = Sets.newHashSet();
		for (Class<?> t : getAllSuperTypes(type)) {
			result.addAll(getConstructors(t, predicates));
		}
		return result;
	}

	/** get constructors of given {@code type}, optionally filtered by {@code predicates} */
	@SafeVarargs
	public static Set<Constructor<?>> getConstructors(Class<?> t, Predicate<? super Constructor<?>>... predicates) {
		// explicit needed only for jdk1.5
		return ReflectionUtils.<Constructor<?>>filter(t.getDeclaredConstructors(), predicates);
	}

	/**
	 * get all fields of given {@code type}, up the super class hierarchy, optionally filtered by
	 * {@code predicates}
	 */
	@SafeVarargs
	public static Set<Field> getAllFields(final Class<?> type, Predicate<? super Field>... predicates) {
		Set<Field> result = Sets.newHashSet();
		for (Class<?> t : getAllSuperTypes(type)) {
			result.addAll(getFields(t, predicates));
		}
		return result;
	}

	/** get fields of given {@code type}, optionally filtered by {@code predicates} */
	@SafeVarargs
	public static Set<Field> getFields(Class<?> type, Predicate<? super Field>... predicates) {
		return filter(type.getDeclaredFields(), predicates);
	}

	/** filter all given {@code elements} with {@code predicates}, if given */
	@SafeVarargs
	public static <T extends AnnotatedElement> Set<T> getAll(final Set<T> elements, Predicate<? super T>... predicates) {
		return predicates.length == 0 ? elements : Sets.newHashSet(Iterables.filter(elements, Predicates.and(predicates)));
	}

	// predicates
	/** where member name equals given {@code name} */
	public static <T extends Member> Predicate<T> withName(final String name) {
		return (@Nullable T input) -> input != null && input.getName().equals(name);
	}

	/** where member name startsWith given {@code prefix} */
	public static <T extends Member> Predicate<T> withPrefix(final String prefix) {
		return (@Nullable T input) -> input != null && input.getName().startsWith(prefix);
	}

	/**
	 * where member name matches given {@code regex}
	 */
	public static <T extends Member> Predicate<T> withNamePattern(final String regex) {
		return (@Nullable T input) -> Pattern.matches(regex, input.getName());
	}

	/**
	 * where member's {@code toString} matches given {@code regex}
	 * <p>
	 * for example:
	 *
	 * <pre>
	 * getAllMethods(someClass, withPattern("public void .*"))
	 * </pre>
	 */
	public static <T extends AnnotatedElement> Predicate<T> withPattern(final String regex) {
		return (@Nullable T input) -> Pattern.matches(regex, input.toString());
	}

	/** when method/constructor parameter types equals given {@code types} */
	public static Predicate<Member> withParameters(final Class<?>... types) {
		return (@Nullable Member input) -> Arrays.equals(parameterTypes(input), types);
	}

	/** when member parameter types assignable to given {@code types} */
	public static Predicate<Member> withParametersAssignableTo(final Class<?>... types) {
		return (@Nullable Member input) -> isAssignable(types, parameterTypes(input));
	}

	/** when method/constructor parameter types assignable from given {@code types} */
	public static Predicate<Member> withParametersAssignableFrom(final Class<?>... types) {
		return (@Nullable Member input) -> isAssignable(parameterTypes(input), types);
	}

	/** when method/constructor parameters count equal given {@code count} */
	public static Predicate<Member> withParametersCount(final int count) {
		return (@Nullable Member input) -> input != null && parameterTypes(input).length == count;
	}

	/** when field type equal given {@code type} */
	public static <T> Predicate<Field> withType(final Class<T> type) {
		return (@Nullable Field input) -> input != null && input.getType().equals(type);
	}

	/** when field type assignable to given {@code type} */
	public static <T> Predicate<Field> withTypeAssignableTo(final Class<T> type) {
		return (@Nullable Field input) -> input != null && type.isAssignableFrom(input.getType());
	}

	/** when method return type equal given {@code type} */
	public static <T> Predicate<Method> withReturnType(final Class<T> type) {
		return (@Nullable Method input) -> input != null && input.getReturnType().equals(type);
	}

	/** when method return type assignable from given {@code type} */
	public static <T> Predicate<Method> withReturnTypeAssignableTo(final Class<T> type) {
		return (@Nullable Method input) -> input != null && type.isAssignableFrom(input.getReturnType());
	}

	/**
	 * when member modifier matches given {@code mod}
	 * <p>
	 * for example:
	 *
	 * <pre>
	 * withModifier(Modifier.PUBLIC)
	 * </pre>
	 */
	public static <T extends Member> Predicate<T> withModifier(final int mod) {
		return (@Nullable T input) -> input != null && (input.getModifiers() & mod) != 0;
	}

	/**
	 * when class modifier matches given {@code mod}
	 * <p>
	 * for example:
	 *
	 * <pre>
	 * withModifier(Modifier.PUBLIC)
	 * </pre>
	 */
	public static Predicate<Class<?>> withClassModifier(final int mod) {
		return (@Nullable Class<?> input) -> input != null && (input.getModifiers() & mod) != 0;
	}

	private static Class<?>[] parameterTypes(Member member) {
		return member != null ? member.getClass() == Method.class ? ((Method) member).getParameterTypes()
				: member.getClass() == Constructor.class ? ((Constructor<?>) member).getParameterTypes() : null : null;
	}

	//
	@SafeVarargs
	static <T> Set<T> filter(final T[] elements, Predicate<? super T>... predicates) {
		return predicates.length == 0 ? Sets.newHashSet(elements)
				: Sets.newHashSet(Iterables.filter(Arrays.asList(elements), Predicates.and(predicates)));
	}

	@SafeVarargs
	static <T> Set<T> filter(final Iterable<T> elements, Predicate<? super T>... predicates) {
		return predicates.length == 0 ? Sets.newHashSet(elements)
				: Sets.newHashSet(Iterables.filter(elements, Predicates.and(predicates)));
	}

	private static boolean isAssignable(Class<?>[] childClasses, Class<?>[] parentClasses) {
		if (childClasses == null) {
			return parentClasses == null || parentClasses.length == 0;
		}
		if (childClasses.length != parentClasses.length) {
			return false;
		}
		for (int i = 0; i < childClasses.length; i++) {
			if (!parentClasses[i].isAssignableFrom(childClasses[i])
					|| parentClasses[i] == Object.class && childClasses[i] != Object.class) {
				return false;
			}
		}
		return true;
	}
}
