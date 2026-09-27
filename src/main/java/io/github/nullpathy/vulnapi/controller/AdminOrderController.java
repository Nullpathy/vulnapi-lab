package io.github.nullpathy.vulnapi.controller;

import io.github.nullpathy.vulnapi.dto.OrderResponse;
import io.github.nullpathy.vulnapi.entity.Order;
import io.github.nullpathy.vulnapi.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAll() {
        List<OrderResponse> orders = orderService.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(orders);
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getProduct().getId(),
                order.getQuantity(),
                order.getCreatedAt()
        );
    }
}