package commandline.exceptions;

public class CommandLineParsingException extends Exception {
    public CommandLineParsingException() {
        super();
    }

    public CommandLineParsingException(String message) {
        super(message);
    }

    public CommandLineParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandLineParsingException(Throwable cause) {
        super(cause);
    }
}