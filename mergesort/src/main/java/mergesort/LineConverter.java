package mergesort;

import mergesort.exceptions.LineConverterException;

public interface LineConverter<T> {
    T convertLine(String line) throws LineConverterException;
}