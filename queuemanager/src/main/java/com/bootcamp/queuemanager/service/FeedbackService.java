package com.bootcamp.queuemanager.service;

import com.bootcamp.queuemanager.dto.CustomerFeedbackDTO;
import com.bootcamp.queuemanager.dto.CustomerFeedbackRequestDTO;
import com.bootcamp.queuemanager.publisher.SNSPublisher;
import com.bootcamp.queuemanager.util.Status;
import com.bootcamp.queuemanager.util.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FeedbackService {
    private final SNSPublisher snsPublisher;
    private final ConcurrentHashMap<Type, LinkedList<CustomerFeedbackDTO>> sqsDataStorage;
    private static final Logger LOG = LoggerFactory.getLogger(FeedbackService.class);
    private final SqsClient sqsClient;
    private final EnumMap<Type, String> sqsUrl;
    private Message message;


    public FeedbackService(SNSPublisher snsPublisher, ConcurrentHashMap<Type, LinkedList<CustomerFeedbackDTO>> sqsDataStorage, SqsClient sqsClient, EnumMap<Type, String> sqsUrl) {
        this.snsPublisher = snsPublisher;
        this.sqsDataStorage = sqsDataStorage;
        this.sqsClient = sqsClient;
        this.sqsUrl = sqsUrl;
    }

    public void sendFeedback(CustomerFeedbackRequestDTO feedback) {
        snsPublisher.sendNotification(feedback);
    }

    private int getSQSSize(Character type) {
        return sqsDataStorage.get(type).size();
    }
    private Map<Type, LinkedList<CustomerFeedbackDTO>> getSQSData() {
        return sqsDataStorage;
    }

    public int getQueueSize(String type) {
        return sqsDataStorage.get(Type.valueOf(type)).size();
    }

    public LinkedList<CustomerFeedbackDTO> getQueueInformation(String type) {
        return sqsDataStorage.get(Type.valueOf(type));
    }

    public void getMessage(String queueUrl) {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(1)
                .build();
        LOG.info("[getMessage] receiveMessageRequest: {}",  receiveMessageRequest);
        ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(receiveMessageRequest);
        LOG.info("[getMessage] receiveMessageResponse: {}",  receiveMessageResponse);
        this.message = receiveMessageResponse.messages().stream().findFirst().orElseThrow();
        LOG.info("[getMessage] this.message: {}",  this.message);
    }

    public Boolean tryGetMessage(String queueUrl) {
        try{
            this.getMessage(queueUrl);
        }
        catch (Exception e){
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public CustomerFeedbackDTO consumeMessage (String tipo) {
        CustomerFeedbackDTO feedback = new CustomerFeedbackDTO(
                "id",
                Status.RECEBIDO,
                Type.valueOf(tipo),
                "mess"
        );
        LOG.info("[consumeMessage] feedback: {}",  feedback);

        LOG.info("[consumeMessage] config.sqsUrl: {}",  sqsUrl);
        String url = sqsUrl.get(Type.valueOf(tipo));
        LOG.info("[consumeMessage] url: {}",  url);
        Boolean ok = this.tryGetMessage(url);
        if (ok) {
            LOG.info("[consumeMessage] feedback: {}",  message.toString());
        }
//        CustomerFeedbackDTO feedback = new CustomerFeedbackDTO(
//                message.MessageId,
//                Status.RECEBIDO,
//                Type.valueOf(tipo),
//                "mess"
//        );
        return feedback;
    }

}
