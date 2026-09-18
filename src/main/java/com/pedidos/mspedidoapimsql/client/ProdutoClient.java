package com.pedidos.mspedidoapimsql.client;

import com.pedidos.mspedidoapimsql.dto.ProdutoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

// cliente rest síncrono pra falar com o produto-api, resolvendo o host pelo nome
// registrado no eureka (RestClient.Builder com @LoadBalanced, ver RestClientConfig)
@Component
public class ProdutoClient {
    private final RestClient restClient;


    public ProdutoClient(@LoadBalanced RestClient.Builder loadBalancedBuilder,
                         @Value("${produto.service.name}") String produtoServiceName) {
        this.restClient = loadBalancedBuilder
                .baseUrl("http://" + produtoServiceName)
                .build();
    }

    // só pra validar (existe o produto? tem estoque?) antes de criar o item, não
    // decide nada sozinho
    public ProdutoDTO buscarPorId(Long id) {
        try {
            return restClient.get()
                    .uri("/produtos/{id}", id)
                    .retrieve()
                    .body(ProdutoDTO.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return null;
            }
            throw new RuntimeException("Falha ao consultar o serviço de produtos: " + e.getMessage(), e);
        }
    }

    // ainda existe pra quem quiser baixar estoque de forma síncrona, mas não é mais
    // chamado no fluxo normal de criar item (ver ItemPedidoService.salvar). hoje a
    // baixa de verdade só acontece via rabbitmq, senão desconta duas vezes
    public ProdutoDTO baixarEstoque(Long id, Integer quantidade){
        try {
            return restClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/produtos/{id}/baixar-estoque")
                            .queryParam("quantidade", quantidade)
                            .build(id))
                    .retrieve()
                    .body(ProdutoDTO.class);
        } catch(RestClientResponseException e){
            throw  new RuntimeException("Falha ao baixar estoque do produto "+id);
        }
    }
}
