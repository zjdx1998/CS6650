package client2;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author jeromy zhang
 * @date Feb-17-22
 */
public class PhaseThread implements Runnable{
    private int startID;
    private int endID;
    private int startTime;
    private int endTime;
    private int numOfReqs;
    private CountDownLatch latch;
    private CountDownLatch overall;
    private SkiersApi api;
    public static final int RETRIES = 5;

    public PhaseThread(int startID, int endID, int startTime, int endTime, int numOfReqs, CountDownLatch latch, CountDownLatch overall) {
        this.startID = startID;
        this.endID = endID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numOfReqs = numOfReqs;
        this.latch = latch;
        this.overall = overall;
        api = new SkiersApi();
        api.getApiClient().setBasePath(Arguments.serverURL);
    }

    @Override
    public void run() {
        for(int i=0;i<numOfReqs;i++){
            int skierID = ThreadLocalRandom.current().nextInt(startID, endID +1);
            int time = ThreadLocalRandom.current().nextInt(startTime, endTime + 1);
            int liftID = ThreadLocalRandom.current().nextInt(Arguments.numLifts) + 1;
            int waitTime = ThreadLocalRandom.current().nextInt(0, 11);
            int curTurn = 0;
            while(curTurn < RETRIES){
                try {
                    long beforeTime = System.currentTimeMillis();
                    ApiResponse<Void> res = api.writeNewLiftRideWithHttpInfo(
                            new LiftRide().liftID(liftID).waitTime(waitTime).time(time),
                            1,
                            "Summer",
                            "1",
                            skierID
                    );
                    long afterTime = System.currentTimeMillis();
                    Arguments.Records.add(new Record(beforeTime, "POST", afterTime-beforeTime, res.getStatusCode()));
                    break;
                } catch (ApiException e){
                    curTurn++;
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            if(curTurn < RETRIES) Arguments.successCount.incrementAndGet();
            else Arguments.failureCount.decrementAndGet();
        }
        latch.countDown();
        overall.countDown();
        Arguments.count.countDown();
    }
}
