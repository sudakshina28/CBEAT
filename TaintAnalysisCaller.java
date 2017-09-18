package analysis;

import org.apache.commons.io.FileUtils;

import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * run taint analysis of given directory
 */
public class TaintAnalysisCaller implements Callable<Boolean> {
    private File targetDirFile;
    private File analysisResult;
    private URL dummyHTMLURL;

    private static TaintAnalysisCaller caller;
    private TaintAnalysisCaller() {
        targetDirFile = null;
        caller = this;
    }

    public static TaintAnalysisCaller v() {
        if (caller == null) {
            caller = new TaintAnalysisCaller();
        }
        return caller;
    }

    private String targetDir;
    public TaintAnalysisCaller(String targetDir) {
        this.targetDir = targetDir;
        targetDirFile = null;
        caller = this;
    }

    private boolean initWithDir(String targetDir) {
        this.targetDirFile = new File(targetDir);
        if (!(targetDirFile.exists() && targetDirFile.isDirectory())) {
            System.out.println("target dir not exist");
            return false;
        }

        Collection<File> jsFiles = FileUtils.listFiles(targetDirFile, new String[]{"js"}, false);
        JavaScriptCallGraph.v().setTaintJsFiles(jsFiles);

        File sourceSinkFile = null, possibleURLsFile = null;
        Collection<File> txtFiles = FileUtils.listFiles(targetDirFile, new String[]{"txt"}, false);
        for (File txtFile : txtFiles) {
            if (txtFile.getName().equals("SourcesAndSinks.txt"))
                sourceSinkFile = txtFile;
            else if (txtFile.getName().equals("possibleURLs.txt"))
                possibleURLsFile = txtFile;
        }

        if (sourceSinkFile == null) {
            System.out.println("cannot find SourcesAndSinks.txt in target dir");
            return false;
        }
        if (possibleURLsFile == null) {
            System.out.println("cannot find possibleURLs.txt in target dir");
            return false;
        }

        try {
            List<String> sourceSinkLines = FileUtils.readLines(sourceSinkFile);
            JavaScriptCallGraph.v().setSourceSinks(sourceSinkLines);

            List<String> possibleURLLines = FileUtils.readLines(possibleURLsFile);
            for (String URLLine : possibleURLLines) {
                URL possibleURL = new URL(URLLine);
                JavaScriptCallGraph.v().addPossibleURL(possibleURL);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File dummyHTMLcp = new File(targetDir, "dummy.html");
        this.analysisResult = FileUtils.getFile(this.targetDirFile, "TaintAnalysis.log");

        try {
            FileUtils.copyURLToFile(getClass().getResource("/dummy.html"), dummyHTMLcp);
            this.dummyHTMLURL = dummyHTMLcp.toURI().toURL();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean run(String targetDir) throws IllegalArgumentException, CancelException, IOException, WalaException {
        if (!initWithDir(targetDir)) {
            System.out.println("initialization failed");
            return false;
        }
        try {
            PrintStream ps = new PrintStream(new FileOutputStream(this.analysisResult));
            JavaScriptCallGraph.v().runTaintAnalysis(dummyHTMLURL, ps);
            JavaScriptCallGraph.v().runTaintAnalysis(ps);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws IllegalArgumentException, CancelException, IOException, WalaException {
        if (args.length != 1) {
            String usage = "usage: HTMLTaintAnalysisCaller.main <targetDir>";
            System.out.println(usage);
            return;
        }
        System.out.println("running HTML taint analysis on dir: " + args[0]);
        TaintAnalysisCaller.v().run(args[0]);
    }

    public static void callWithTimeOut(String targetDir, int timeoutSeconds) {
        TaintAnalysisCaller htmlTaintAnalysisCaller = new TaintAnalysisCaller(targetDir);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(htmlTaintAnalysisCaller);

        try {
            System.out.println("HTML taint analysis started!");
            if (future.get(timeoutSeconds, TimeUnit.SECONDS)) {
                System.out.println("HTML taint analysis finished!");
            }
            else {
                System.out.println("HTML taint analysis failed");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.out.println("HTML taint analysis failed!");
            future.cancel(true);
        } catch (TimeoutException e) {
            System.out.println("HTML taint analysis timeout!");
            future.cancel(true);
        }
        executor.shutdownNow();
    }

    @Override
    public Boolean call() throws Exception {
        return run(this.targetDir);
    }
}
