package org.cryptomator.common;

@FunctionalInterface
public interface SupplierThrowingException<ResultType,ErrorType extends Throwable> {
	
	ResultType get() throws ErrorType;
	
}
