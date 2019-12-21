package pr2.exceptions;

public class NoSuchFileException extends Exception {

	private static final long serialVersionUID = 8627422878429089290L;

	public NoSuchFileException(String message)
	{
		super(message);
	}

}
