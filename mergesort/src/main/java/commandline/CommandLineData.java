package commandline;

public class CommandLineData {
    private boolean isAscendingOrder = false;
    private boolean isDescendingOrder = false;
    private boolean isStringFile = false;
    private boolean isNumberFile = false;
    private boolean isHelpRequired = false;


    private String outputFilename;
    private String[] inputFiles;

    public boolean isAscendingOrder() {
        return isAscendingOrder;
    }

    void setAscendingOrder() {
        isAscendingOrder = true;
        isDescendingOrder = false;
    }

    public boolean isDescendingOrder() {
        return isDescendingOrder;
    }

    void setDescendingOrder() {
        isDescendingOrder = true;
        isAscendingOrder = false;
    }

    public boolean isStringFile() {
        return isStringFile;
    }

    void setStringFile() {
        isStringFile = true;
        isNumberFile = false;
    }

    public boolean isNumberFile() {
        return isNumberFile;
    }

    void setNumberFile() {
        isNumberFile = true;
        isStringFile = false;
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }

    public String[] getInputFiles() {
        return inputFiles;
    }

    void setInputFiles(String[] inputFiles) {
        this.inputFiles = inputFiles;
    }

    public boolean isHelpRequired() {
        return isHelpRequired;
    }

    public void setHelpRequired() {
        isHelpRequired = true;
    }
}
