package com.bootcamp.queuemanager.controller;

import com.bootcamp.queuemanager.model.CustomerFeedbackRequest;
import com.bootcamp.queuemanager.service.FeedbackProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackProducerService feedbackProducerService;

    /** Enviar Feedback **/
    @PostMapping("/envio")
    public ResponseEntity<String> enviarFeedback(@RequestBody CustomerFeedbackRequest feedback) {
        feedbackProducerService.sendFeedback(feedback);
        return ResponseEntity.ok("Feedback enviado!");
    }

    /** Obter tamanho atual da fila de feedbacks para cada tipo **/
    @GetMapping("/tamanho")
    public ResponseEntity<String> obterTamanhoDaFila(@RequestParam String tipo) {
        int tamanho = 0;
            //TODO chamada da service
        return ResponseEntity.ok("A fila do tipo " + tipo + " possui tamanho " + tamanho);
    }

    /** Obter informações sobre todos os feedbacks na fila de cada tipo **/
    @GetMapping("/info")
    public ResponseEntity<List<CustomerFeedbackRequest>> obterInformacoesFeedbacks (@RequestParam String tipo) {
        List<CustomerFeedbackRequest> feedbacks = new ArrayList<>();
            //TODO chamada da service, utilizar MAPA cuja chave é a inicial do tipo da fila
        return ResponseEntity.ok(feedbacks);
    }
}
