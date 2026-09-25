package io.github.nullpathy.vulnapi.controller;

import io.github.nullpathy.vulnapi.dto.CreateOrderRequest;
import io.github.nullpathy.vulnapi.dto.OrderResponse;
import io.github.nullpathy.vulnapi.entity.Order;
import io.github.nullpathy.vulnapi.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        Order order = orderService.createOrder(
                request.userId(),
                request.productId(),
                request.quantity()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAll() {
        List<OrderResponse> orders = orderService.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        return orderService.findById(id)
                .map(order -> ResponseEntity.ok(toResponse(order)))
                .orElseGet(() -> ResponseEntity.notFound().build());
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