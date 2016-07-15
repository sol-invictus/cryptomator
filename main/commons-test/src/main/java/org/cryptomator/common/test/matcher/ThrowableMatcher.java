package org.cryptomator.common.test.matcher;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;

import org.hamcrest.Matcher;

public class ThrowableMatcher {

	public static Matcher<Throwable> throwableWithCauseThat(Matcher<? super Throwable> subMatcher) {
		return new PropertyMatcher<>(Throwable.class, Throwable::getCause, "cause", subMatcher);
	}
	
	public static Matcher<Throwable> throwableWithCause(Throwable cause) {
		return throwableWithCauseThat(is(cause));
	}
	
	public static Matcher<Throwable> throwableWithCauseOfType(Class<? extends Throwable> type) {
		return throwableWithCauseThat(is(instanceOf(type)));
	}
	
}
