package com.kovalev007.jacoco;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;

public class Jacoco {

    private static final List<String> FOLDERS_FOR_SKIP = Arrays.asList(".git", ".metadata", ".recommenders", "parent", "target", "db");
    private static final String sep = File.separator;
    private static final String title = "JACOCO HTML REPORT";

    private List<File> dataFiles;
    private String mainProjectFolderStr;
    private String classesFolderStr;
    private String sourcesFolderStr;
    private File outputFolder;

    public Jacoco(List<File> dataFiles, String mainProjectFolder, String classesFolder, String sourcesFolder, File outputFolder) {
        this.dataFiles = dataFiles;
        this.mainProjectFolderStr = mainProjectFolder;
        this.classesFolderStr = classesFolder;
        this.sourcesFolderStr = sourcesFolder;
        this.outputFolder = outputFolder;
    }

    public void create() throws IOException {
        removeOldReport();
        generateNewReport();
    }

    private void removeOldReport() throws IOException {
        FileUtils.deleteDirectory(outputFolder);
    }

    private void generateNewReport() throws IOException {
        ExecFileLoader execFileLoader = loadExecutionData();
        List<String> subProjects = getSubProjects(mainProjectFolderStr);
        List<JacocoBundle> jacocoBundles = analyzeStructureAll(execFileLoader, subProjects, mainProjectFolderStr, classesFolderStr, sourcesFolderStr);
        createReport(execFileLoader, jacocoBundles);
    }

    private ExecFileLoader loadExecutionData() throws IOException {
        ExecFileLoader execFileLoader = new ExecFileLoader();
        for (File dataFile : dataFiles) {
            execFileLoader.load(dataFile);
        }
        return execFileLoader;
    }

    private List<String> getSubProjects(String mainProjectFolderStr) {
        List<String> subProjects = new ArrayList<String>();

        File mainProjectFolder = new File(mainProjectFolderStr);
        File[] filesList = mainProjectFolder.listFiles();
        for (File f : filesList) {
            if (f.isDirectory()) {
                if (!FOLDERS_FOR_SKIP.contains(f.getName())) {
                    subProjects.add(f.getName());
                }
            }
        }

        return subProjects;
    }

    private List<JacocoBundle> analyzeStructureAll(ExecFileLoader execFileLoader, List<String> subProjects, String mainProjectFolderStr, String classesFolderStr, String sourcesFolderStr) throws IOException {
        List<JacocoBundle> jacocoBundles = new ArrayList<JacocoBundle>();
        for (String subProject : subProjects) {
            IBundleCoverage bundleCoverage = analyzeStructure(execFileLoader, subProject, new File(mainProjectFolderStr + sep + subProject + sep + classesFolderStr));
            JacocoBundle jacocoBundle = new JacocoBundle();
            jacocoBundle.setBundleCoverage(bundleCoverage);
            jacocoBundle.setSourceFolderStr(mainProjectFolderStr + sep + subProject + sep + sourcesFolderStr);
            jacocoBundles.add(jacocoBundle);
        }
        return jacocoBundles;
    }

    private IBundleCoverage analyzeStructure(ExecFileLoader execFileLoader, String name, File file) throws IOException {
        CoverageBuilder coverageBuilder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);

        analyzer.analyzeAll(file);

        return coverageBuilder.getBundle(name);
    }

    private void createReport(ExecFileLoader execFileLoader, List<JacocoBundle> jacocoBundles) throws IOException {
        HTMLFormatter htmlFormatter = new HTMLFormatter();
        IReportVisitor visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(outputFolder));

        visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(), execFileLoader.getExecutionDataStore().getContents());

        IReportGroupVisitor iReportGroupVisitor = visitor.visitGroup(title);
        for (JacocoBundle jacocoBundle : jacocoBundles) {
            iReportGroupVisitor.visitBundle(jacocoBundle.getBundleCoverage(), new DirectorySourceFileLocator(new File(jacocoBundle.getSourceFolderStr()), "utf-8", 4));
        }

        visitor.visitEnd();
    }

    private class JacocoBundle {

        private IBundleCoverage bundleCoverage;
        private String sourceFolderStr;

        public IBundleCoverage getBundleCoverage() {
            return bundleCoverage;
        }

        public void setBundleCoverage(IBundleCoverage bundleCoverage) {
            this.bundleCoverage = bundleCoverage;
        }

        public String getSourceFolderStr() {
            return sourceFolderStr;
        }

        public void setSourceFolderStr(String sourceFolderStr) {
            this.sourceFolderStr = sourceFolderStr;
        }

    }

}