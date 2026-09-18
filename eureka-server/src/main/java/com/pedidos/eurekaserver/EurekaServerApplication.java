package com.pedidos.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

// Servidor de descoberta de serviços (service discovery). O mspedido-api-msql e o
// produto-api-mysql são "eureka clients": eles se registram aqui ao subir, e é aqui
// que o mspedido-api descobre o endereço real do produto-api para as chamadas REST
// de validação de estoque (ProdutoClient), sem precisar de um endereço fixo.
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
