package org.cryptomator.common.test.matcher;

import java.lang.reflect.Field;
import java.util.Optional;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;

public class PrivateFieldMatcher extends DiagnosingMatcher<Object> {

	public static <T> Builder<T> hasField(String name, Class<T> type) {
		return matcher -> new PrivateFieldMatcher(name, type, matcher);
	}
	
	private final String name;
	private final Class<?> type;
	private final Matcher<?> matcher;
	
	public PrivateFieldMatcher(String name, Class<?> type, Matcher<?> matcher) {
		this.name = name;
		this.type = type;
		this.matcher = matcher;
	}

	@Override
	public void describeTo(Description description) {
		description //
			.appendText("an object that has a field ") //
			.appendValue(name) //
			.appendText(" of type ") //
			.appendText(type.getSimpleName()) //
			.appendText(" that ") //
			.appendDescriptionOf(matcher);
	}

	@Override
	protected boolean matches(Object item, Description mismatchDescription) {
		if (item == null) {
			mismatchDescription.appendText("a null value");
			return false;
		}
		Optional<Field> field = findField(item.getClass());
		if (field.isPresent()) {
			return fieldMatches(item, mismatchDescription, field);
		} else {
			mismatchDescription.appendText("an object that has no field ").appendText(name);
			return false;
		}
	}

	private boolean fieldMatches(Object item, Description mismatchDescription, Optional<Field> field) {
		if (!field.get().getType().equals(type)) {
			mismatchDescription
				.appendText("an object that has a field ")
				.appendText(name)
				.appendText(" of type ")
				.appendText(field.get().getType().getSimpleName());
			return false;
		}
		try {
			return typeCheckedFieldMatches(item, mismatchDescription, field);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Failed to access field", e);
		}
	}

	private boolean typeCheckedFieldMatches(Object item, Description mismatchDescription, Optional<Field> field)
			throws IllegalAccessException {
		field.get().setAccessible(true);
		Object value = field.get().get(item);
		if (matcher.matches(value)) {
			return true;
		} else {
			mismatchDescription //
				.appendText("an object that has a field ") //
				.appendValue(name) //
				.appendText(" of type ") //
				.appendText(type.getSimpleName()) //
				.appendText(" that not ") //
				.appendDescriptionOf(matcher);
			return false;
		}
	}
	
	private Optional<Field> findField(Class<?> clazz) {
		if (clazz == null) {
			return Optional.empty();
		}
		try {
			return Optional.of(clazz.getDeclaredField(name));
		} catch (NoSuchFieldException e) {
			return findField(clazz.getSuperclass());
		}
	}

	public interface Builder<T> {
		
		PrivateFieldMatcher that(Matcher<? super T> matcher);
		
	}
	
}
