package com.bootcamp.queuemanager.util;

import com.bootcamp.queuemanager.dto.CustomerFeedbackDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.model.Message;

public class Utilities {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static CustomerFeedbackDTO messageToDTO(Message message) throws JsonProcessingException {
        String messageBody = message.body();
        JsonNode jsonNode = objectMapper.readTree(messageBody).get("Message");
        String strMessage = jsonNode.asText();
        CustomerFeedbackDTO customerFeedbackDTO = objectMapper.readValue(strMessage, CustomerFeedbackDTO.class);
        return customerFeedbackDTO;
    }
}
