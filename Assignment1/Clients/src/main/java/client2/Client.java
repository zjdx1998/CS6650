package client2;

import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jeromy zhang
 * @date Feb-17-22
 */
public class Client {

    private static ExecutorService pool;

    public static void main(String[] args) throws IOException, InterruptedException {
        new Arguments().parser(args);
        SkiersApi apiInstance = new SkiersApi();
        apiInstance.getApiClient().setBasePath(Arguments.serverURL);
        if(Arguments.isTestOnly){
            testLatency();
            return;
        }
        LiftRide ride = new LiftRide().time(1).liftID(1).waitTime(1);
        try {
            apiInstance.writeNewLiftRide(ride, 56, "2022", "200", 1);
            System.out.println("Success");
        } catch (Exception e) {
            e.printStackTrace();
        }

        int NumPhaseOneThreads = Arguments.numThreads / 4;
        int NumPhaseTwoThreads = Arguments.numThreads;
        int NumPhaseThreeThreads = Arguments.numThreads / 10;
        pool = Executors.newFixedThreadPool(NumPhaseOneThreads + NumPhaseTwoThreads + NumPhaseThreeThreads);
        Arguments.count = new CountDownLatch(NumPhaseOneThreads + NumPhaseTwoThreads + NumPhaseThreeThreads);
        System.out.println("Ready to run phases!");
        long startTime = System.currentTimeMillis();
        doPhase("Phase1", 0.2, NumPhaseOneThreads, 1, 90, (int)(Arguments.numRuns * 0.2 * Arguments.numSkiers/NumPhaseOneThreads));
        doPhase("Phase2", 0.2, NumPhaseTwoThreads, 91, 360, (int)(Arguments.numRuns * 0.6 * Arguments.numSkiers/NumPhaseTwoThreads));
        doPhase("Phase3", 1.0, NumPhaseThreeThreads, 361, 420, (int)(Arguments.numRuns * 0.1));
        Arguments.count.countDown();
        pool.shutdown();
        long endTime = System.currentTimeMillis();
        System.out.println(
                "Number of Successful Requests Sent: " + Arguments.successCount.get() + "\n"
                + "Number of Unsuccessful Requests: " + Arguments.failureCount.get() + "\n"
                + "Total run time: " + String.valueOf(endTime-startTime) + " (ms) \n"
                + "Total Throughput in requests per second: " + 1000.0 * (Arguments.failureCount.get() + Arguments.successCount.get())/(endTime-startTime)
        );
        new RecordUtilities("./res/records/" + Arguments.numThreads + "_" + Arguments.numSkiers + ".csv").calculateOutput();
        System.exit(0);
    }

    private static void testLatency() throws InterruptedException {
        System.out.println("Ready to test latency!");
        long startTime = System.currentTimeMillis();
        CountDownLatch testLatch = new CountDownLatch(1);
        Arguments.count = new CountDownLatch(1);
        CountDownLatch overall = new CountDownLatch(1);
        new PhaseThread(1, 10001, 1, 420, 1000, testLatch).run();
        testLatch.await();
        long endTime = System.currentTimeMillis();
        System.out.println("Total Duration is " + (endTime - startTime) + " with average latency about " + 1.0*(endTime - startTime)/10000);
    }

    private static void doPhase(String phaseName, double percent, int numPhaseThreads, int startTime, int endTime, int numOfReqs) throws InterruptedException {
        System.out.println(phaseName + " is ready to start!");
        System.out.println(phaseName + " should execute " + numPhaseThreads + " threads with " + numOfReqs + " requests each.");
        CountDownLatch latch = new CountDownLatch((int)Math.ceil(numPhaseThreads * percent));
        for(int i=0;i<numPhaseThreads;i++){
            int startID = (int)( 1.0 * Arguments.numSkiers / numPhaseThreads * 1.0 * i + 1),
                    endID = (int)( 1.0 * Arguments.numSkiers / numPhaseThreads * (i + 1) );
            pool.execute(new PhaseThread(startID, endID, startTime, endTime, numOfReqs, latch));
        }
        latch.await();
        System.out.println(phaseName + " has already completed " + String.valueOf(percent*100) + "% tasks");
    }
}