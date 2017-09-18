package analysis;

import com.ibm.wala.cast.ipa.callgraph.CAstAnalysisScope;
import com.ibm.wala.cast.ipa.callgraph.CAstCallGraphUtil;
import com.ibm.wala.cast.ipa.callgraph.StandardFunctionTargetSelector;
import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.ir.translator.TranslatorToCAst;
import com.ibm.wala.cast.js.html.WebPageLoaderFactory;
import com.ibm.wala.cast.js.html.WebUtil;
import com.ibm.wala.cast.js.ipa.callgraph.*;
import com.ibm.wala.cast.js.ipa.callgraph.correlations.extraction.CorrelatedPairExtractorFactory;
import com.ibm.wala.cast.js.loader.JavaScriptLoader;
import com.ibm.wala.cast.js.loader.JavaScriptLoaderFactory;
import com.ibm.wala.cast.js.test.JSCallGraphBuilderUtil;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.cast.js.translator.JavaScriptTranslatorFactory;
import com.ibm.wala.cast.loader.CAstAbstractLoader;
import com.ibm.wala.cast.tree.rewrite.CAstRewriterFactory;
import com.ibm.wala.cast.types.AstMethodReference;
import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.warnings.Warning;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;

/**
 */
public class JavaScriptCallGraph {
    private Collection<File> taintJsFiles;
    private Collection<URL> possibleURLs;
    private Collection<SourceSink> sourceSinks;

    public static JavaScriptCallGraph v() {
        if (webManager == null) {
            webManager = new JavaScriptCallGraph();
        }
        return webManager;
    }

    private static JavaScriptCallGraph webManager = null;
    private JavaScriptCallGraph() {
        taintJsFiles = new HashSet<>();
        possibleURLs = new HashSet<>();
        sourceSinks = new HashSet<>();
    }

    public void setTaintJsFiles(Collection<File> taintJsFiles) {
        this.taintJsFiles = taintJsFiles;
    }

    public void addPossibleURL(URL possible_url) {
        possibleURLs.add(possible_url);
    }

    public void setSourceSinks(Collection<String> htmlSourceSinks) {
        for (String htmlSourceSinkStr : htmlSourceSinks) {
            this.sourceSinks.add(new SourceSink(htmlSourceSinkStr));
        }
    }

    public void runTaintAnalysis(PrintStream ps) {
        ps.println("analysis result:");
        for (URL possible_url : possibleURLs) {
            ps.println("\n\nrunning taint analysis");
            try {
                runTaintAnalysis(possible_url, ps);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("analysis failed on URL: " + possible_url.toString());
            }
        }
    }

    public void runTaintAnalysis(URL url, PrintStream ps) throws IllegalArgumentException, CancelException, IOException, WalaException {
        CallGraph cg = generateCG(url, taintJsFiles);
        if (cg == null) {
            System.out.println("generating CG failed on URL: " + url.toString());
            return;
        }
        TaintAnalysis jsTaint = new TaintAnalysis(cg, sourceSinks);
        jsTaint.analyze();
        jsTaint.dumpResult(ps);
       // printIRs(cg);
    }

    public static void printIRs(CallGraph cg) throws ClassHierarchyException, IOException {
    	for(CGNode n : cg) {
            IR ir = n.getIR();
            System.out.println(ir);
        }
    	}

    public CallGraph generateCG(URL possible_url, Collection<File> jsFiles) throws IllegalArgumentException, IOException, CancelException, WalaException {
         
            //System.out.println("Analyzing " + possible_url.toString());
    	 JavaScriptTranslatorFactory javaScriptTranslatorFactory = new CAstRhinoTranslatorFactory();
         JSCallGraphUtil.setTranslatorFactory(javaScriptTranslatorFactory);
        JSCallGraphBuilderUtil.CGBuilderType builderType = JSCallGraphBuilderUtil.CGBuilderType.ZERO_ONE_CFA;
            IRFactory<IMethod> irFactory = AstIRFactory.makeDefaultFactory();
            Set<SourceModule> scripts = HashSetFactory.make();
            scripts.add(JSCallGraphUtil.getPrologueFile("prologue.js"));
            for (File jsFile : jsFiles) {
                try {
                    scripts.add(CAstCallGraphUtil.makeSourceModule(jsFile.toURI().toURL(), jsFile.getName()));
                } catch (Exception ignored) {
                }
            }
            SourceModule[] scriptsArray = scripts.toArray(new SourceModule[scripts.size()]);
            
            
       	String dir="dir";
            String filename="main.js";
            CallGraph cg =JSCallGraphBuilderUtil.makeScriptCG(scriptsArray,builderType,irFactory);
             System.out.println(cg);
        return cg;
    }
}
