package cmsc420.exception;

public class WalrusErrorException extends Throwable {
	private static final long serialVersionUID = -6878077114302943595L;

	public WalrusErrorException() {
	}

	public WalrusErrorException(String message) {
		super(message);
	}
}