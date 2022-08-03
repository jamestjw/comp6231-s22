import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;

public class Main {
	static void main(String[] args) {
           JavaSparkContext sc = new JavaSparkContext();
           JavaRDD<String> lines = ctx.textFile("StudentsPerformance.csv");
           JavaRDD<String> words = lines.flatMap(
             new FlatMapFunction<String, String>() {
               public Iterable<String> call(String s) {
                 return Arrays.asList(s.split(" "));
               }
             }
           );
	}
}
