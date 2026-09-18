package com.pedidos.mspedidoapimsql.messaging;

// mensagem que o mspedido-api publica e o produto-api consome, pedindo pra baixar
// "quantidade" do produto "produtoId". carrega pedidoId e itemId só pra o produto-api
// devolver essas referências na resposta (ResultadoEstoque), já que ele não guarda
// pedido/item, só processa e responde
public class BaixarEstoqueCommand {

    private Long pedidoId;
    private Long itemId;
    private Long produtoId;
    private Integer quantidade;

    public BaixarEstoqueCommand() {
    }

    public BaixarEstoqueCommand(Long pedidoId, Long itemId,
                                Long produtoId, Integer quantidade) {
        this.pedidoId = pedidoId;
        this.itemId = itemId;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
