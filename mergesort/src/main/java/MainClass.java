
import commandline.CommandLineData;
import commandline.exceptions.CommandLineParsingException;
import commandline.CommandLineProcessor;
import mergesort.exceptions.FileMergerException;
import mergesort.exceptions.LineConverterException;
import mergesort.FileSorter;
import mergesort.LineConverter;

import java.util.Comparator;


public class MainClass {
    private static final int FAILURE_EXIT_CODE = 1;

    private static final Comparator<String> stringComparator = String::compareTo;
    private static final Comparator<Long> numberComparator = Long::compareTo;

    private static final LineConverter<String> stringExtractor = line -> line;
    private static final LineConverter<Long> numberExtractor = line -> {
        try {
            return Long.parseLong(line);
        } catch (NumberFormatException e) {
            throw new LineConverterException(e);
        }
    };

    public static void main(String[] args) {
        try {
            CommandLineData commandLineData = CommandLineProcessor.processCommandLine(args);
            if (commandLineData.isHelpRequired()) {
                CommandLineProcessor.printUsage();
                return;
            }

            FileSorter<?> fileSorter;
            if (commandLineData.isStringFile()) {
                fileSorter = new FileSorter<>(stringExtractor, commandLineData.isAscendingOrder() ?
                                                            stringComparator : stringComparator.reversed());
            } else {
                fileSorter = new FileSorter<>(numberExtractor, commandLineData.isAscendingOrder() ?
                                                            numberComparator : numberComparator.reversed());
            }

            fileSorter.mergeSortedFiles(commandLineData.getOutputFilename(), commandLineData.getInputFiles());
        } catch (CommandLineParsingException e) {
            System.err.println(e.getMessage());
            CommandLineProcessor.printUsage();
            System.exit(FAILURE_EXIT_CODE);
        } catch (FileMergerException e) {
            System.err.println(e.getMessage());
            System.exit(FAILURE_EXIT_CODE);
        }
    }
}