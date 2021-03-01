package nl.koppeltaal.poc.fhir.exception;

/**
 *
 */
public class EnitityNotFoundException extends RuntimeException {

	public EnitityNotFoundException() {
	}

	public EnitityNotFoundException(String message) {
		super(message);
	}

	public EnitityNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public EnitityNotFoundException(Throwable cause) {
		super(cause);
	}

	public EnitityNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
