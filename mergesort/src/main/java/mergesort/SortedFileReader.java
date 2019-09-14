package mergesort;

import mergesort.exceptions.LineConverterException;

import java.io.*;
import java.util.Comparator;

public class SortedFileReader<T> implements Closeable {
    private File file;
    private BufferedReader fileReader;

    private long invalidLines = 0;
    private LineConverter<T> lineConverter;

    private long unorderedLines = 0;
    private Comparator<T> lineComparator;

    private T currentConvertedLine;
    private String currentLine;

    SortedFileReader(File file, LineConverter<T> lineConverter, Comparator<T> lineComparator) throws IOException {
        fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        this.file = file;
        this.lineComparator = lineComparator;
        this.lineConverter = lineConverter;

        moveToNextValidLine();
    }

    boolean hasReachedEnd() {
        return currentLine == null;
    }

    boolean isValid() {
        return invalidLines == 0;
    }

    long getInvalidLinesNumber() {
        return invalidLines;
    }

    boolean isOrdered() {
        return unorderedLines == 0;
    }

    long getUnorderedLinesNumber() {
        return unorderedLines;
    }

    String getFilepath() {
        return file.getPath();
    }

    String getCurrentLine() {
        return currentLine;
    }

    T getCurrentConvertedLine() {
        return currentConvertedLine;
    }

    void moveToNextLineInOrder() throws IOException {
        T prevConvertedLine = currentConvertedLine;
        moveToNextValidLine();
        while (currentConvertedLine != null && lineComparator.compare(prevConvertedLine, currentConvertedLine) > 0) {
            ++unorderedLines;
            moveToNextValidLine();
        }
    }

    private void moveToNextValidLine() throws IOException {
        for (;;) {
            currentLine = fileReader.readLine();
            if (hasReachedEnd()) {
                currentConvertedLine = null;
                return;
            }

            try {
                currentConvertedLine = lineConverter.convertLine(currentLine);
                return;
            } catch (LineConverterException e) {
                ++invalidLines;
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (fileReader != null) {
            fileReader.close();
        }
    }
}
