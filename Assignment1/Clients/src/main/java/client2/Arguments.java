package client2;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jeromy zhang
 * @date Feb-17-22
 */
public class Arguments {
    @Option(name="-nt",required = true, usage="maximum number of threads to run (numThreads - max 1024)")
    public static int numThreads;

    @Option(name="-ns", required = true, usage="number of skier to generate lift rides for (numSkiers - max 100000), This is effectively the skier’s ID (skierID)")        // no usage
    public static int numSkiers;

    @Option(name="-nl",required = true, hidden=false, usage="number of ski lifts (numLifts - range 5-60, default 40)")
    public static int numLifts = 40;

    @Option(name="-nr",required = true, usage="mean numbers of ski lifts each skier rides each day (numRuns - default 10, max 20)")
    public static int numRuns = 10;

    @Option(name="-server", required = true, usage="IP/port address of the server")
    public static String serverHost;

    @Option(name="-test-only", usage = "set test latency mode")
    public static boolean isTestOnly = false;

    @Argument
    private List<String> arguments = new ArrayList<String>();
    public static List<Record> Records = new ArrayList<>();

    public static String serverURL;

    public static final AtomicInteger successCount = new AtomicInteger(0);
    public static final AtomicInteger failureCount = new AtomicInteger(0);
    public static CountDownLatch count;

    public void parser(String[] args){
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
            serverURL = "http://" + serverHost;
            if(!serverHost.contains("localhost")) serverURL += "/Server";
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java Client [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();
            System.err.println("  Example: java Client -mt 1024 -nl 40 -nr 20 -nt 1000 -server ec2-54-149-212-65.us-west-2.compute.amazonaws.com");
        }
    }
}
