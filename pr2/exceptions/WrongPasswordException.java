package pr2.exceptions;

public class WrongPasswordException extends Exception {

	private static final long serialVersionUID = 4437581626763583614L;

	public WrongPasswordException(String message)
	{
		super(message);
	}

}
