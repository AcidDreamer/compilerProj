
import ast.interfaces.*;
import ast.specifics.*;
import ast.visitors.*;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
public class Compiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Compiler.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            LOGGER.info("Usage : java Compiler [ --encoding <name> ] <inputfile(s)>");
        } else {
            int firstFilePos = 0;
            String encodingName = "UTF-8";
            if (args[0].equals("--encoding")) {
                firstFilePos = 2;
                encodingName = args[1];
                try {
                    java.nio.charset.Charset.forName(encodingName); // Side-effect: is encodingName valid? 
                } catch (Exception e) {
                    LOGGER.error("Invalid encoding '" + encodingName + "'");
                    return;
                }
            }
            for (int i = firstFilePos; i < args.length; i++) {
                Lexer scanner = null;
                try {
                    java.io.FileInputStream stream = new java.io.FileInputStream(args[i]);
                    LOGGER.info("Scanning file " + args[i]);
                    java.io.Reader reader = new java.io.InputStreamReader(stream, encodingName);
                    scanner = new Lexer(reader);
                    // parse
                    parser p = new parser(scanner);
                    ASTNode compUnit = (ASTNode) p.parse().value;
                    LOGGER.debug("Constructed AST");

                    // keep global instance of program
                    Registry.getInstance().setRoot(compUnit);

                    // build symbol table
                    LOGGER.info("\n<=========Building system table=========>\n");
                    compUnit.accept(new SymTableBuilderASTVisitor());
                    LOGGER.debug("\n<=========Building local variables index=========>\n");
                    compUnit.accept(new LocalIndexBuilderASTVisitor());

                    // construct types
                    LOGGER.info("\n<=========Semantic check=========>\n");
                    compUnit.accept(new CollectSymbolsASTVisitor());
                    LOGGER.info("\n<=========Type Collector =========>\n");
                    compUnit.accept(new CollectTypesASTVisitor());

                  // print program
                  LOGGER.info("\n<=========Input=========>\n");
                  ASTVisitor printVisitor = new PrintASTVisitor();
                  compUnit.accept(printVisitor);
                  
                  // print 3-address code
                    LOGGER.info("\n<=========3-address code:=========>\n");
                    IntermediateCodeASTVisitor threeAddrVisitor = new IntermediateCodeASTVisitor();
                    compUnit.accept(threeAddrVisitor);
                    String intermediateCode = threeAddrVisitor.getProgram().emit();
                    System.out.println(intermediateCode);

                    // convert to java bytecode
                    LOGGER.info("\n<=========Bytecode:=========>\n");
                    BytecodeGeneratorASTVisitor bytecodeVisitor = new BytecodeGeneratorASTVisitor();
                    LOGGER.info("WAS HERE!");

                    compUnit.accept(bytecodeVisitor);
                    LOGGER.info("WAS HERE!");
                    ClassNode cn = bytecodeVisitor.getClassNode();
                    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
                    TraceClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
                    cn.accept(cv);
                    // get code
                    byte code[] = cw.toByteArray();

                    // update to file
                    LOGGER.info("/n<=========Writing class to file NotACalculator.class:=========>\n");
                    FileOutputStream fos = new FileOutputStream("NoACalculator.class");
                    fos.write(code);
                    fos.close();
                    LOGGER.info("Compilation done");

                    // instantiate class
                    LOGGER.info("Loading class NoACalculator.class");
                    ReloadingClassLoader rcl = new ReloadingClassLoader(ClassLoader.getSystemClassLoader());
                    rcl.register("NoACalculator", code);
                    Class<?> calculatorClass = rcl.loadClass("NoACalculator");

                    // run main method
                    // Method meth = calculatorClass.getMethod("main", String[].class);
                    // String[] params = null;
                    // LOGGER.info("Executing");
                    // meth.invoke(null, (Object) params);

                  LOGGER.info("Compilation done");
                } catch (java.io.FileNotFoundException e) {
                    LOGGER.error("File not found : \"" + args[i] + "\"");
                } catch (java.io.IOException e) {
                    LOGGER.error("IO error scanning file \"" + args[i] + "\"");
                    LOGGER.error(e.toString());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage() + "\n");
                    e.printStackTrace();
                    //e.printStackTrace();
                }
            }
        }
    }

}
