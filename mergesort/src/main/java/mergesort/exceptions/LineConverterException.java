package mergesort.exceptions;

public class LineConverterException extends Exception {
    public LineConverterException() {
        super();
    }

    public LineConverterException(String message) {
        super(message);
    }

    public LineConverterException(String message, Throwable cause) {
        super(message, cause);
    }

    public LineConverterException(Throwable cause) {
        super(cause);
    }
}