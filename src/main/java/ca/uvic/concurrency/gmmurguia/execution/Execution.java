package ca.uvic.concurrency.gmmurguia.execution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 0, time = 1)
@Measurement(iterations = 10, batchSize = 1)
//@Measurement(iterations = 1, batchSize = 1)
@State(Scope.Benchmark)
@Fork(value = 5)
public class Execution {

    private static Logger logger = LogManager.getLogger();

    @Param({"1"})
    public String command;

    public static void main(String[] args) throws IOException, RunnerException {
        Yaml yaml = new Yaml();
        try(InputStream in = Execution.class.getClassLoader().getResourceAsStream("config.yaml")) {
            ArrayList<String> commands = new ArrayList<>();
            Map<String, ArrayList<HashMap<String, Object>>> config = yaml.loadAs(in, Map.class);
            for (Map<String, Object> problems : config.get("problems")) {
                String problemName = problems.get("name").toString();
                logger.info("Problem: " + problemName);

                List implementations = (List<String>) problems.get("implementations");
                for (Object implPropsObj : implementations) {
                    Map<String, String> implProps = (Map<String, String>) implPropsObj;
                    String lang = implProps.get("lang");
                    String finalDir = problemName + "/" + lang;
                    File problem = new File(finalDir);
                    problem.mkdirs();
                    logger.info("Language of the solution: " + lang);
                    logger.info("Executing command: " + implProps.get("command"));

                    commands.add(implProps.get("command"));
                }
            }

            String[] commandsArray = new String[commands.size()];
            Options opt = new OptionsBuilder()
                    .include(Execution.class.getSimpleName())
                    .param("command", commands.toArray(commandsArray))
                    .build();
            new Runner(opt).run();
        }
    }

    public static void executeCommand(String command) {
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine())!= null) {
                logger.info(line);
            }

            p.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Benchmark
    public void execute() {
//        executeCommand("ex2.sh " + command);
        executeCommand(command);
    }

}
