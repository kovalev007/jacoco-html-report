package com.kovalev007.jacoco;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JacocoApp {

    private static final String ARGS_ERROR_MESSAGE = "Expected command line arguments: <data_files_comma_separated_string> <main_project_folder> <classes_folder> <sources_folder> <output_folder>";

    private static List<String> dataFilesStr;
    private static List<File> dataFiles = new ArrayList<File>();
    private static String mainProjectFolder;
    private static String classesFolder;
    private static String sourcesFolder;
    private static String outputFolderStr;
    private static File outputFolder;

    public static void main(final String[] args) throws IOException {
        if (args.length != 5) {
            throw new IllegalArgumentException(ARGS_ERROR_MESSAGE);
        }

        dataFilesStr = Arrays.asList(args[0].split(","));
        for (String dataFileStr : dataFilesStr) {
            File dataFile = new File(dataFileStr);
            dataFiles.add(dataFile);
        }

        mainProjectFolder = args[1];
        classesFolder = args[2];
        sourcesFolder = args[3];

        outputFolderStr = args[4];
        outputFolder = new File(outputFolderStr);

        Jacoco jacoco = new Jacoco(dataFiles, mainProjectFolder, classesFolder, sourcesFolder, outputFolder);
        jacoco.create();
    }

}