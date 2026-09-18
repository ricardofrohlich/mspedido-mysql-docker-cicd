package com.pedidos.mspedidoapimsql.messaging;

// mensagem que o produto-api publica de volta, dizendo se a baixa foi confirmada
// (true) ou recusada (false, com o motivo). é essa mensagem que o
// ResultadoEstoqueListener usa pra decidir se o pedido vira PROCESSADO ou CANCELADO
public class ResultadoEstoque {

    private Long pedidoId;
    private Long itemId;
    private Long produtoId;
    private boolean confirmado;
    private String motivo;

    public ResultadoEstoque() {
    }

    public ResultadoEstoque(
            Long pedidoId,
            Long itemId,
            Long produtoId,
            boolean confirmado,
            String motivo) {

        this.pedidoId = pedidoId;
        this.itemId = itemId;
        this.produtoId = produtoId;
        this.confirmado = confirmado;
        this.motivo = motivo;
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

    public boolean isConfirmado() {
        return confirmado;
    }

    public void setConfirmado(boolean confirmado) {
        this.confirmado = confirmado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
