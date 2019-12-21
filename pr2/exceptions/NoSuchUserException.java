package pr2.exceptions;

public class NoSuchUserException extends Exception {

	private static final long serialVersionUID = 6617893207973551227L;

	public NoSuchUserException(String message)
	{
		super(message);
	}

}
