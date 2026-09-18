package com.pedidos.mspedidoapimsql.service;

import com.pedidos.mspedidoapimsql.model.ItemPedido;
import com.pedidos.mspedidoapimsql.model.Pedido;
import com.pedidos.mspedidoapimsql.model.StatusPedido;
import com.pedidos.mspedidoapimsql.repository.ItemPedidoRepository;
import com.pedidos.mspedidoapimsql.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoService {
    private final PedidoRepository repository;
    private final ItemPedidoRepository itemPedidoRepository;

    public PedidoService(PedidoRepository repository, ItemPedidoRepository itemPedidoRepository) {
        this.repository = repository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    public Pedido salvar(Pedido pedido){
        // quando cria o pedido sem lista de itens (o normal é criar vazio e ir
        // adicionando pelo /itens depois), pedido.getItens() vem null - o for antigo
        // dava NullPointerException nesse caso
        if (pedido.getItens() != null) {
            for (ItemPedido itemPedido : pedido.getItens()) {
                itemPedido.setPedido(pedido);
            }
        }
        return repository.save(pedido);
    }

    public List<Pedido> listar(){
        return repository.findAll();
    }
    public List<Pedido> buscarPorStatus(StatusPedido status){
        return repository.findByStatus(status);
    }

    // soma o subtotal de cada item e atualiza o valor total do pedido. isso é síncrono,
    // diferente da baixa de estoque que agora é via rabbit
    public void recalcularValor(Long pedidoId){
        Pedido pedido = repository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: id "+pedidoId));

        Double total = itemPedidoRepository.somarSubtotalPorPedido(pedidoId);
        pedido.setValor(total);
        repository.save(pedido);
    }

    // chamado pelo ResultadoEstoqueListener quando chega a resposta do produto-api,
    // atualiza o status pra PROCESSADO (confirmado) ou CANCELADO (recusado)
    public void atualizarStatus(Long pedidoId, StatusPedido status){
        Pedido pedido = repository.findById(pedidoId)
                .orElseThrow(() ->
                        new RuntimeException("Pedido não encontrado"));

        pedido.setStatus(status);

        repository.save(pedido);
    }
}
