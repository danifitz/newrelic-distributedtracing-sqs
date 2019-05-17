import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;

import java.util.Calendar;
import java.util.List;

public class SQSQueue {

    public static String QUEUE_NAME = "TEST_QUEUE";

    private static AmazonSQS sqs = AmazonSQSClientBuilder.standard()
            .withRegion("eu-west-2")
            .build();
    private static String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();

    public static void main(String[] args) {
        SQSQueue queue = new SQSQueue();
        System.out.println(Calendar.getInstance().getTime().toString() + " - " + queue.getMessageFromQueue());

        while(true) {
            synchronized (queue) {
                queue.waitMethod();
            }
        }
    }

    @Trace(dispatcher = true)
    public String getMessageFromQueue() {
        // Make a receive message request from the SQS queue URL
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest().withQueueUrl(queueUrl);
        // Receive the message from AWS SQS
        List<Message> messages = sqs.receiveMessage(receiveMessageRequest.withMessageAttributeNames("dtPayload")).getMessages();
        // Get the first message
        Message message = messages.get(0);
        String messageValue = message.getBody();

        // Get the Distributed Trace payload from the SQS Message
        String dtPayload = message.getMessageAttributes().get("dtPayload").getStringValue();

        // Accept the dt payload
        System.out.println(dtPayload);
        NewRelic.getAgent().getTransaction().acceptDistributedTracePayload(dtPayload);

        return messageValue;
    }

    private void waitMethod() {
        try {
            this.wait(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}