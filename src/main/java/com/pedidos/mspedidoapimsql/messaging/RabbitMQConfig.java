package com.pedidos.mspedidoapimsql.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import java.util.Map;

// config do rabbit do lado do mspedido-api (quem pede a baixa e escuta o resultado).
// o mesmo exchange "pedidos.exchange" também é declarado no produto-api - de boa, os
// dois lados podem declarar o mesmo exchange/fila, o rabbit só garante que existem
// antes de usar
@Configuration
public class RabbitMQConfig {

    // exchange topic: as mensagens são roteadas pras filas que tiverem um binding cuja
    // routing key bate com a da mensagem. aqui uso routing key exata (sem * ou #), então
    // na prática funciona tipo um roteamento direto por assunto
    public static final String EXCHANGE = "pedidos.exchange";

    // routing key pra PEDIR a baixa (mspedido -> produto)
    public static final String ROUTING_KEY_BAIXAR_ESTOQUE =
            "estoque.baixar";

    // routing keys que o produto-api usa pra responder se aceitou...
    public static final String ROUTING_KEY_CONFIRMADO =
            "estoque.confirmado";

    // ...ou recusou (estoque insuficiente)
    public static final String ROUTING_KEY_RECUSADO =
            "estoque.recusado";

    // fila onde o mspedido-api recebe a resposta (confirmado ou recusado). só uma fila
    // pros dois casos, quem decide o que fazer é o ResultadoEstoqueListener olhando o
    // campo "confirmado"
    public static final String QUEUE_RESULTADO_ESTOQUE =
            "pedido.resultado-estoque.queue";


    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }


    @Bean
    public Queue queueResultadoEstoque() {
        return new Queue(QUEUE_RESULTADO_ESTOQUE);
    }


    // a fila de resultado escuta as duas routing keys: confirmado...
    @Bean
    public Binding bindingResultadoConfirmado() {
        return BindingBuilder
                .bind(queueResultadoEstoque())
                .to(exchange())
                .with(ROUTING_KEY_CONFIRMADO);
    }


    // ...e recusado
    @Bean
    public Binding bindingResultadoRecusado() {
        return BindingBuilder
                .bind(queueResultadoEstoque())
                .to(exchange())
                .with(ROUTING_KEY_RECUSADO);
    }

    // conversor json pras mensagens do rabbit. sem isso o spring amqp tenta usar
    // serialização java por padrão, que exige "implements Serializable" e ainda quebra
    // porque as classes de mensagem dos dois serviços vivem em pacotes diferentes. com
    // json além de funcionar entre pacotes/serviços diferentes, dá pra ver a mensagem
    // direto no painel do rabbitmq (porta 15672)
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        // por padrão o conversor grava o nome completo (com pacote) da classe java de
        // quem publicou no header "__TypeId__" e tenta usar esse mesmo nome pra
        // desserializar do outro lado - não rola aqui pq cada serviço tem sua própria
        // cópia das classes de mensagem, em pacotes diferentes. então mapeio um apelido
        // curto e igual nos dois serviços pra cada tipo de mensagem (mesmo mapeamento
        // no RabbitMQConfig do produto-api)
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setIdClassMapping(Map.of(
                "baixarEstoqueCommand", BaixarEstoqueCommand.class,
                "resultadoEstoque", ResultadoEstoque.class
        ));
        converter.setClassMapper(classMapper);

        return converter;
    }
}
