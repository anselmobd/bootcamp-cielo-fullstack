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
    private final EnumMap<Type, String> sqsType2Url;

    private Message message;


    public FeedbackService(SNSPublisher snsPublisher, ConcurrentHashMap<Type, LinkedList<CustomerFeedbackDTO>> sqsDataStorage, SqsClient sqsClient, EnumMap<Type, String> sqsType2Url) {
        this.snsPublisher = snsPublisher;
        this.sqsDataStorage = sqsDataStorage;
        this.sqsClient = sqsClient;
        this.sqsType2Url = sqsType2Url;
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

    public CustomerFeedbackDTO consumeMessage (String tipoMsg) {
        CustomerFeedbackDTO feedback = new CustomerFeedbackDTO(
            "id",
            Status.RECEBIDO,
            Type.valueOf(tipoMsg),
            "mess"
        );
        LOG.info("[consumeMessage] feedback: {}",  feedback);

        LOG.info("[consumeMessage] config.sqsType2Url: {}",  sqsType2Url);
        String url = sqsType2Url.get(Type.valueOf(tipoMsg));
        LOG.info("[consumeMessage] url: {}",  url);
        Boolean ok = this.tryGetMessage(url);
        if (ok) {
            LOG.info("[consumeMessage] message.toString: {}",  message.toString());
            LOG.info("[consumeMessage] message.attributes: {}",  message.messageAttributes());
        }
        feedback = new CustomerFeedbackDTO(
            message.messageId(),
            Status.RECEBIDO,
            Type.valueOf(tipoMsg),
            "message"
        );
        return feedback;
    }

}
