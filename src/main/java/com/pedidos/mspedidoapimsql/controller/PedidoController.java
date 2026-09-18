package com.pedidos.mspedidoapimsql.controller;

import com.pedidos.mspedidoapimsql.model.Pedido;
import com.pedidos.mspedidoapimsql.model.StatusPedido;
import com.pedidos.mspedidoapimsql.service.PedidoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    private final PedidoService service;

    public PedidoController(PedidoService service) {
                this.service = service;
    }


    @PostMapping
    public Pedido salvar(@RequestBody Pedido pedido){
        return service.salvar(pedido);
    }

    @GetMapping
    public List<Pedido> listar(){
        return service.listar();
    }
    @GetMapping("/status/{status}")
    public List<Pedido> buscarPorStatus(@PathVariable StatusPedido status){
        return service.buscarPorStatus(status);
    }
}

