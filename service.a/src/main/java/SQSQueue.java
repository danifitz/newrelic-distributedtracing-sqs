import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;

import java.util.Calendar;

public class SQSQueue {

    public static String QUEUE_NAME = "TEST_QUEUE";

    private static AmazonSQS sqs = AmazonSQSClientBuilder.standard()
            .withRegion("eu-west-2")
            //.withCredentials()
            .build();
    private static String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();

    private NewRelic nr = new NewRelic();

    public static void main(String[] args) {

        SQSQueue queue = new SQSQueue();
        queue.addMessageToQueue("Hey There, this is App A");
        while(true) {
            synchronized (queue) {
                queue.waitMethod();
            }
        }
    }

    // Adding the @Trace annotation with dispatcher = true tells New Relic to start a new Transaction
    @Trace(dispatcher = true)
    private void addMessageToQueue(String message) {
        // Get a distributed trace payload to include with the SQS message so we can see a trace
        String dtPayload = NewRelic.getAgent().getTransaction().createDistributedTracePayload().text();
        System.out.println(dtPayload);

        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(message)
                .addMessageAttributesEntry("dtPayload",
                        new MessageAttributeValue().withDataType("String").withStringValue(dtPayload))
                .withDelaySeconds(5);

        sqs.sendMessage(send_msg_request);
    }

    private void waitMethod() {
        try {
            System.out.println(Calendar.getInstance().getTime().toString());
            this.wait(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
