package mergesort.exceptions;

public class FileMergerException extends Exception {
    public FileMergerException() {
        super();
    }

    public FileMergerException(String message) {
        super(message);
    }

    public FileMergerException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileMergerException(Throwable cause) {
        super(cause);
    }
}
