package com.pedidos.mspedidoapimsql.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

// quem publica a mensagem pedindo baixa de estoque. não fala direto com o produto-api,
// só entrega pro exchange e o rabbit cuida de levar até quem tiver escutando a routing
// key certa - isso é o desacoplamento, o mspedido-api nem precisa saber que o
// produto-api existe pra publicar isso
@Component
public class EstoquePublisher {

    private final RabbitTemplate rabbitTemplate;

    public EstoquePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarBaixaEstoque(BaixarEstoqueCommand comando) {

        // serializa o comando (em json, configurado no RabbitMQConfig) e publica no
        // exchange "pedidos.exchange" com routing key "estoque.baixar"
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_BAIXAR_ESTOQUE,
                comando
        );
    }
}
