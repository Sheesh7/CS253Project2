package JobSchedulerTest;

import JobScheduler.CSJobScheduler;
import JobScheduler.CSJobSchedulerInterface.FinishedJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Konstantinos Ameranis
 * Tests CSJobScheduler
 */
public class CSJobSchedulerTest {
    final static String prefix = System.getenv("ASNLIB") + "/test/JobScheduler/";
    final static File[] folderPaths = {new File(prefix + "sample"),
            new File(prefix + "testFiles"),
//            new File(prefix + "generated")
    };
    final static String inputPrefix = "input", ansPrefix = "output";

    /**
     * All done in one instance of the manager
     */
    CSJobScheduler scheduler = new CSJobScheduler();


    /**
     * Provides a list of test files and their names for the parameterized test below.
     * @return List of valid test input files and their names
     */
    static Stream<Arguments> testFileProvider(){
        ArrayList<Arguments> args = new ArrayList<>();
        //for all folders provided
        for(final File path : folderPaths){
            //for each file in each folder
            for(final File entry : Objects.requireNonNull(path.listFiles())){
                String inputFile = entry.getPath();
                //if not an input file, skip
                if(! (inputFile.substring(0, inputPrefix.length()).equalsIgnoreCase(inputPrefix))){
                    continue;
                }
                args.add(arguments(Named.of(entry.getName(), entry)));
            }
        }
        return args.stream();
    }


    /**
     * Runs all input files
     */
    @DisplayName("File-based tests for Part 4")
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("testFileProvider")
    void runFiles(File file){
        String inputFile = file.getPath();
        String failurePrefix = "Test case \"" + inputFile + "\":: ";

        //guaranteed to have a valid input file
        String ansFile = ansPrefix + inputFile.substring(inputPrefix.length(), inputFile.length());

        //run test
        ArrayList<FinishedJob<?, ?, ?>> ans = null;
        try {
            ans = scheduler.getCompletionTimes(inputFile);
        } catch(Exception e){
            e.printStackTrace();
            fail("Error calling scheduler.getCompletionTimes(\"" + file.getName() + "\": " + e.getMessage());
        }

        //compare to answer

        //read in answer file
        BufferedReader bf = null;
        try {
            bf = new BufferedReader(new FileReader(ansFile));
        } catch (FileNotFoundException e) {
            fail("GRADER ERROR:: ANSWER FILE NOT FOUND:: \"" + file.getName() + "\"");
        }

        int jobIndex = 0;
        int lineCount = 0;
        String line;
        try {
            while ((line = bf.readLine()) != null) {
                //written out explicitly for clarity for future readers of this code
                line = line.strip();
                lineCount++;
                if (jobIndex > ans.size()) {
                    fail(failurePrefix + "\nIncorrect size, only " + ans.size() + " jobs returned.");
                }

                String currentLine = ans.get(jobIndex++).toString();
                if (!line.equals(currentLine)) {
                    fail(failurePrefix + "\nIncorrect element at line " + jobIndex + ",\nexpected \"" + ans + "\"\nactual is \"" + currentLine + "\"");
                }
            }
            if (lineCount != ans.size()) {
                fail(failurePrefix + "Additional entries found in answer.\nActual answer has " + lineCount + " entries, solution returned " + ans.size());
            }
        } catch (IOException e) {
            fail(failurePrefix + e.toString());
        }
    }
}

