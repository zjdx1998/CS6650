package client1;
import io.swagger.client.*;
import io.swagger.client.api.*;
import org.kohsuke.args4j.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Client {

    @Option(name="-mt",required = true, usage="maximum number of threads to run (numThreads - max 1024)")
    private int numThreads;

    @Option(name="-nt", required = true, usage="number of skier to generate lift rides for (numSkiers - max 100000), This is effectively the skier’s ID (skierID)")        // no usage
    private int numSkiers;

    @Option(name="-nl",required = true, hidden=false, usage="number of ski lifts (numLifts - range 5-60, default 40)")
    private int numLifts = 40;

    @Option(name="-nr",required = true, usage="mean numbers of ski lifts each skier rides each day (numRuns - default 10, max 20)")
    private int num = 10;

    @Option(name="-server", required = true, usage="IP/port address of the server")
    private String serverHost;

    private String serverURL;

    // receives other command line parameters than options
    @Argument
    private List<String> arguments = new ArrayList<String>();

    public static void main(String[] args) throws IOException {
        new Client().doMain(args);
    }

    public void doMain(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            // parse the arguments.
            parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java Client [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();
            // print option sample. This is useful some time
            System.err.println("  Example: java Client -mt 1024 -nl 40 -nr 20 -nt 1000 -server ec2-54-149-212-65.us-west-2.compute.amazonaws.com");
            return;
        }

        serverURL = "http://" + serverHost + "/Server";
        System.out.println(serverURL);
    }
}