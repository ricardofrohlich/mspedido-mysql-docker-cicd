package com.pedidos.mspedidoapimsql.messaging;

import com.pedidos.mspedidoapimsql.model.StatusPedido;
import com.pedidos.mspedidoapimsql.service.PedidoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// fica escutando a fila de resultado e reage quando o produto-api confirma ou recusa
// a baixa de estoque - é aqui que fecha o ciclo assíncrono
@Component
public class ResultadoEstoqueListener {

    private final PedidoService pedidoService;

    public ResultadoEstoqueListener(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // o spring amqp chama esse método sozinho toda vez que chega mensagem na fila
    // (numa thread separada da requisição http que criou o item)
    @RabbitListener(queues = RabbitMQConfig.QUEUE_RESULTADO_ESTOQUE)
    public void receber(ResultadoEstoque resultado) {

        System.out.println("Resultado do estoque recebido!");
        System.out.println("Pedido: " + resultado.getPedidoId());
        System.out.println("Produto: " + resultado.getProdutoId());

        if (resultado.isConfirmado()) {

            System.out.println("Estoque confirmado!");

            pedidoService.atualizarStatus(
                    resultado.getPedidoId(),
                    StatusPedido.PROCESSADO
            );

        } else {

            System.out.println("Estoque recusado!");
            System.out.println("Motivo: " + resultado.getMotivo());

            pedidoService.atualizarStatus(
                    resultado.getPedidoId(),
                    StatusPedido.CANCELADO
            );
        }
    }
}
