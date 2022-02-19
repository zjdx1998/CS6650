package client2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

public class RecordUtilities {
    private FileWriter csvWriter;
    private String filePath;
    public RecordUtilities(String filePath) throws IOException {
        this.filePath = filePath;
        csvWriter = new FileWriter(filePath);
        csvWriter.append("startTime,requestType,latency,responseCode");
    }

    public void addRecordToCSV(Record r) throws IOException {
        csvWriter.append(r.toString());
    }

    public void calculateOutput() throws IOException {
        Collections.sort(Arguments.Records);
        double min = 1000000, max = 0, sum = 0;
        double median = Arguments.Records.get((int)(0.5*Arguments.Records.size())).getLatency();
        double p99 = Arguments.Records.get((int)(0.99*Arguments.Records.size())).getLatency();
        for(Record r : Arguments.Records){
            sum += r.getLatency();
            max = Math.max(max, r.getLatency());
            min = Math.min(min, r.getLatency());
            addRecordToCSV(r);
        }
        double mean = sum / Arguments.Records.size();
        double throughput = Arguments.Records.size() / sum * 1000;
        System.out.println(
                "Mean response time: " + mean + "\n"
                +"Median response time: " + median + "\n"
                +"Throughput: " + throughput + "\n"
                +"99th response time" + p99 + "\n"
                +"min and max response time" + "min: " + min + " , max: " + max
        );
    }
}
