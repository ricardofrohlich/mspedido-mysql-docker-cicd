package com.pedidos.mspedidoapimsql.service;


import com.pedidos.mspedidoapimsql.client.ProdutoClient;
import com.pedidos.mspedidoapimsql.dto.ProdutoDTO;
import com.pedidos.mspedidoapimsql.messaging.BaixarEstoqueCommand;
import com.pedidos.mspedidoapimsql.messaging.EstoquePublisher;
import com.pedidos.mspedidoapimsql.model.ItemPedido;
import com.pedidos.mspedidoapimsql.repository.ItemPedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemPedidoService {
    private final ItemPedidoRepository repository;
    private final ProdutoClient produtoClient;
    private final PedidoService pedidoService;
    private final EstoquePublisher estoquePublisher;

    public ItemPedidoService(ItemPedidoRepository repository, ProdutoClient produtoClient, PedidoService pedidoService, EstoquePublisher estoquePublisher) {
        this.repository = repository;
        this.produtoClient = produtoClient;
        this.pedidoService = pedidoService;
        this.estoquePublisher = estoquePublisher;
    }

    public ItemPedido salvar(ItemPedido item){
        // consulta síncrona só pra validar rapidinho: o produto existe e, pelo último
        // dado do produto-api, tem estoque suficiente. é só um feedback rápido, quem
        // decide de verdade é o produto-api quando processa a mensagem do rabbit
        ProdutoDTO produto = produtoClient.buscarPorId(item.getProdutoId());
        if(produto == null){
            throw new RuntimeException("Produto "+item.getProdutoId() + " não foi encontrado");
        }
        if(produto.getEstoque() == null || produto.getEstoque() < item.getQuantidade()){
            throw new RuntimeException("Estoque insuficiente para o produto "+produto.getNome());
        }
        item.setSubtotal(produto.getPreco() * item.getQuantidade());

        // tinha uma chamada rest síncrona aqui antes de publicar no rabbit, que
        // descontava o estoque duas vezes (uma aqui, outra quando o produto-api
        // consumia a mensagem). removi a chamada rest, agora a baixa acontece só uma
        // vez, via rabbit mesmo
        ItemPedido itemSalvo = repository.save(item);

        // publica no rabbit o comando pedindo a baixa. não espera resposta aqui, só vai
        // saber se confirmou ou recusou quando o ResultadoEstoqueListener receber a
        // mensagem de volta e atualizar o status do pedido
        estoquePublisher.publicarBaixaEstoque(
                new BaixarEstoqueCommand(itemSalvo.getPedido().getId(),
                        itemSalvo.getId(),
                        itemSalvo.getProdutoId(),
                        itemSalvo.getQuantidade()
                )
        );
        pedidoService.recalcularValor(itemSalvo.getPedido().getId());
        return itemSalvo;
    }

    public List<ItemPedido> listar(){
        return repository.findAll();
    }

    public ItemPedido buscarPorId(Long id){
        return repository.findById(id).orElseThrow(() ->  new RuntimeException("Item não encontrado"));
    }

    public void deletar(Long id){
        ItemPedido item = buscarPorId(id); //chego aqui eu so tenho o id do itemPedido
        Long pedidoId = item.getPedido().getId();//com ele eu busco o pedido e com o id do pedido eu chamo
        repository.deleteById(id);
        pedidoService.recalcularValor(pedidoId); //recalcular
    }
}
