package commandline;

import commandline.exceptions.CommandLineParsingException;
import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.List;

public class CommandLineProcessor {
    private static final String STRING_DATATYPE_OPTION = "s";
    private static final String STRING_DATATYPE_OPTION_DESCRIPTION = "file contains strings";

    private static final String INTEGER_DATATYPE_OPTION = "i";
    private static final String INTEGER_DATATYPE_OPTION_DESCRIPTION = "file contains numbers";

    private static final String ASCENDING_ORDER_OPTION = "a";
    private static final String ASCENDING_ORDER_OPTION_DESCRIPTION = "sort in ascending order (by default)";

    private static final String DESCENDING_ORDER_OPTION = "d";
    private static final String DESCENDING_ORDER_OPTION_DESCRIPTION = "sort in descending order";

    private static final String HELP_OPTION = "h";
    private static final String HELP_OPTION_LONG = "help";
    private static final String HELP_OPTION_DESCRIPTION = "show help";

    private static final Options options = buildOptions();
    private static final Options optionsForHelpCheck = buildOptionsForHelpCheck();
    private static final HelpFormatter helpFormatter = new HelpFormatter();
    private static final String commandLineSyntax =
            "java -jar <appName>.jar" + " <output_file> [input_file_1, ...]";

    public static void printUsage() {
        helpFormatter.printHelp(commandLineSyntax, options);
    }

    public static CommandLineData processCommandLine(String[] args) throws CommandLineParsingException {
        try {
            CommandLineData commandLineData = new CommandLineData();
            if (checkForHelp(args)) {
                commandLineData.setHelpRequired();
                return commandLineData;
            }

            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(options, args);

            commandLineData.setAscendingOrder();
            if (commandLine.hasOption(DESCENDING_ORDER_OPTION)) {
                commandLineData.setDescendingOrder();
            }

            commandLineData.setStringFile();
            if (commandLine.hasOption(INTEGER_DATATYPE_OPTION)) {
                commandLineData.setNumberFile();
            }

            List<String> fileList = commandLine.getArgList();
            if (fileList.size() <= 1) {
                throw new CommandLineParsingException("List of files has to contain exactly one output file" +
                        " and at least one input file");
            }

            String outputFilename = fileList.remove(0);
            String[] inputFilenames = fileList.toArray(String[]::new);
            commandLineData.setOutputFilename(outputFilename);
            commandLineData.setInputFiles(inputFilenames);
            return commandLineData;

        } catch (UnrecognizedOptionException e) {
            throw new CommandLineParsingException("Unrecognized option " + e.getOption(), e);
        } catch (MissingOptionException e) {
            throw new CommandLineParsingException("One of the following options is required: " +
                    Arrays.toString(e.getMissingOptions().toArray()), e);
        } catch (ParseException e) {
            throw new CommandLineParsingException("Unexpected parsing exception", e);
        }
    }

    private static boolean checkForHelp(String[] args) throws ParseException {
        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = commandLineParser.parse(optionsForHelpCheck, args);
        return commandLine.hasOption(HELP_OPTION) || commandLine.hasOption(HELP_OPTION_LONG);
    }

    private static Options buildOptions() {
        OptionGroup datatypeOptions = new OptionGroup();
        datatypeOptions.setRequired(true);
        datatypeOptions.addOption(new Option(STRING_DATATYPE_OPTION, STRING_DATATYPE_OPTION_DESCRIPTION));
        datatypeOptions.addOption(new Option(INTEGER_DATATYPE_OPTION, INTEGER_DATATYPE_OPTION_DESCRIPTION));

        OptionGroup orderOptions = new OptionGroup();
        orderOptions.setRequired(false);
        orderOptions.addOption(new Option(ASCENDING_ORDER_OPTION, ASCENDING_ORDER_OPTION_DESCRIPTION));
        orderOptions.addOption(new Option(DESCENDING_ORDER_OPTION, DESCENDING_ORDER_OPTION_DESCRIPTION));

        Options options = new Options();
        options.addOptionGroup(datatypeOptions);
        options.addOptionGroup(orderOptions);
        options.addOption(HELP_OPTION, HELP_OPTION_LONG, false, HELP_OPTION_DESCRIPTION);

        return options;
    }

    private static Options buildOptionsForHelpCheck() {
        Options options = new Options();
        options.addOption(STRING_DATATYPE_OPTION, STRING_DATATYPE_OPTION_DESCRIPTION);
        options.addOption(INTEGER_DATATYPE_OPTION, INTEGER_DATATYPE_OPTION_DESCRIPTION);
        options.addOption(ASCENDING_ORDER_OPTION, ASCENDING_ORDER_OPTION_DESCRIPTION);
        options.addOption(DESCENDING_ORDER_OPTION, DESCENDING_ORDER_OPTION_DESCRIPTION);
        options.addOption(HELP_OPTION, HELP_OPTION_LONG, false, HELP_OPTION_DESCRIPTION);

        return options;
    }
}
