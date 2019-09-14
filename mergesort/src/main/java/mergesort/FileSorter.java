package mergesort;

import mergesort.exceptions.FileMergerException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileSorter<T> {
    private Comparator<T> lineComparator;
    private LineConverter<T> lineConverter;

    public FileSorter(LineConverter<T> lineConverter, Comparator<T> lineComparator) {
        this.lineComparator = lineComparator;
        this.lineConverter = lineConverter;
    }

    public void mergeSortedFiles(String outputFilename, String... inputFilenames) throws FileMergerException {
        try {
            List<File> inputFiles = filterExistingFiles(inputFilenames);
            if (inputFiles.isEmpty()) {
                throw new FileMergerException("Error: number of valid input files is <= 1");
            }

            File resultFile = new File(outputFilename);
            resultFile.createNewFile();

            if (inputFiles.size() == 1) {
                copyFile(inputFiles.get(0), resultFile);
                return;
            }

            mergeSortedFilesImpl(resultFile, inputFiles);
        } catch (IOException e) {
            throw new FileMergerException(e);
        }
    }

    private static List<File> filterExistingFiles(String... inputFilenames) {
        List<File> inputFiles = new ArrayList<>();
        for (String filename : inputFilenames) {
            File inputFile = new File(filename);
            if (!inputFile.exists()) {
                System.err.printf("Warning: file \"%s\" does not exist\n", filename);
                continue;
            }
            if (!inputFile.isFile()) {
                System.err.printf("Warning: file \"%s\" is not a regular file\n", filename);
                continue;
            }
            inputFiles.add(inputFile);
        }

        return inputFiles;
    }

    private void mergeSortedFilesImpl(File resultFile, List<File> inputFiles) throws FileMergerException {
        Comparator<File> fileComparator = Comparator.comparingLong(File::length);
        PriorityQueue<File> filesOrderedByLength = new PriorityQueue<>(fileComparator);
        filesOrderedByLength.addAll(inputFiles);

        List<File> temporaryFiles = new ArrayList<>();
        ExecutorService executor = Executors.newCachedThreadPool();
        AtomicBoolean errorHasOccurred = new AtomicBoolean(false);

        try {
            for (int mergersRemaining = inputFiles.size() - 1; mergersRemaining > 0; --mergersRemaining) {
                File firstFile, secondFile;
                synchronized (filesOrderedByLength) {
                    while (filesOrderedByLength.size() <= 1) {
                        if (errorHasOccurred.get()) {
                            return;
                        }
                        filesOrderedByLength.wait();
                    }

                    firstFile = filesOrderedByLength.poll();
                    secondFile = filesOrderedByLength.poll();
                }

                File outputFile;
                if (mergersRemaining == 1) {
                    outputFile = resultFile;
                } else {
                    outputFile = File.createTempFile("mergesort", ".tmp");
                    temporaryFiles.add(outputFile);
                }

                executor.submit(() -> {
                    try {
                        mergeTwoFiles(outputFile, firstFile, secondFile);
                        synchronized (filesOrderedByLength) {
                            filesOrderedByLength.add(outputFile);
                            filesOrderedByLength.notify();
                        }
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                        synchronized (filesOrderedByLength) {
                            errorHasOccurred.set(true);
                            filesOrderedByLength.notify();
                        }
                    }
                });
            }
        } catch (IOException e) {
            errorHasOccurred.set(true);
            throw new FileMergerException(e);
        } catch (InterruptedException e) {
            errorHasOccurred.set(true);
            Thread.currentThread().interrupt();
            throw new FileMergerException(e);
        } finally {
            if (errorHasOccurred.get()) {
                executor.shutdownNow();
            } else {
                executor.shutdown();
            }

            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                errorHasOccurred.set(true);
            }

            if (errorHasOccurred.get()) {
                System.err.println("File merger process was interrupted or an error has occurred. " +
                        "The result may have been incorrect.");
            }

            for (File tempFile : temporaryFiles) {
                tempFile.delete();
            }
        }
    }

    private void copyFile(File srcFile, File destFile) throws IOException {
        try (
                SortedFileReader<T> srcFileReader = new SortedFileReader<>(srcFile, lineConverter, lineComparator);
                PrintWriter destFileWriter = new PrintWriter(destFile)
        ) {
            while (!srcFileReader.hasReachedEnd()) {
                transferLine(destFileWriter, srcFileReader);
            }
            checkFileForCorrectness(srcFileReader);
        }
    }

    private void mergeTwoFiles(File resultFile, File firstFile, File secondFile) throws IOException {
        try (
                SortedFileReader<T> firstSortedFile = new SortedFileReader<>(firstFile, lineConverter, lineComparator);
                SortedFileReader<T> secondSortedFile = new SortedFileReader<>(secondFile, lineConverter, lineComparator);
                PrintWriter resultFileWriter = new PrintWriter(resultFile)
        ) {
            while (!firstSortedFile.hasReachedEnd() && !secondSortedFile.hasReachedEnd()) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                T firstCurrentLine = firstSortedFile.getCurrentConvertedLine();
                T secondCurrentLine = secondSortedFile.getCurrentConvertedLine();
                if (lineComparator.compare(firstCurrentLine, secondCurrentLine) < 0) {
                    transferLine(resultFileWriter, firstSortedFile);
                } else {
                    transferLine(resultFileWriter, secondSortedFile);
                }
            }

            SortedFileReader<T> remainingFile = (firstSortedFile.hasReachedEnd()) ? secondSortedFile : firstSortedFile;
            while (!remainingFile.hasReachedEnd()) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                transferLine(resultFileWriter, remainingFile);
            }

            checkFileForCorrectness(firstSortedFile);
            checkFileForCorrectness(secondSortedFile);
        }
    }

    private void transferLine(PrintWriter destFileWriter, SortedFileReader<T> srcFileReader) throws IOException {
        destFileWriter.println(srcFileReader.getCurrentLine());
        srcFileReader.moveToNextLineInOrder();
    }

    private void checkFileForCorrectness(SortedFileReader<T> inputFile) {
        if (!inputFile.isValid()) {
            System.err.printf("Warning: file \"%s\" contains %d invalid lines; " +
                    "some data have been lost\n", inputFile.getFilepath(), inputFile.getInvalidLinesNumber());
        }

        if (!inputFile.isOrdered()) {
            System.err.printf("Warning: file \"%s\" contains %d lines that are out of order; " +
                    "some data have been lost\n", inputFile.getFilepath(), inputFile.getUnorderedLinesNumber());
        }
    }
}