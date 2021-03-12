package cvetkovic.exceptions;

public class UnreachableCodeDetectedException extends RuntimeException {
    public UnreachableCodeDetectedException(String message) {
        super(message);
    }
}
