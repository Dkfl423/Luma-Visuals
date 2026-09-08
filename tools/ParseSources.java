import com.sun.source.util.JavacTask;
import javax.tools.*;
import java.nio.file.*;
import java.util.*;
public class ParseSources {
 public static void main(String[] args) throws Exception {
  JavaCompiler c=ToolProvider.getSystemJavaCompiler();
  if(c==null) throw new IllegalStateException("A full JDK is needed");
  DiagnosticCollector<JavaFileObject> d=new DiagnosticCollector<>();
  try(var fm=c.getStandardFileManager(d,null,java.nio.charset.StandardCharsets.UTF_8);var paths=Files.walk(Path.of(args[0]))){
   var files=paths.filter(p->p.toString().endsWith(".java")).toList();
   var input=fm.getJavaFileObjectsFromPaths(files);
   JavacTask task=(JavacTask)c.getTask(null,fm,d,List.of("--release","21","-proc:none"),null,input);
   for(var unit:task.parse()) { }
   long errors=d.getDiagnostics().stream().filter(x->x.getKind()==Diagnostic.Kind.ERROR).count();
   d.getDiagnostics().forEach(System.out::println);
   if(errors>0)throw new AssertionError(errors+" syntax errors");
   System.out.println("PASS: Parsed "+files.size()+" Java files using the Java 21 grammar. No type-check / Minecraft compilation claimed.");
  }
 }
}
