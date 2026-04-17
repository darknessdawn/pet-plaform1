package com.pet.controller;

import com.pet.common.Result;
import com.pet.dto.OrderCreateDTO;
import com.pet.service.OrderService;
import com.pet.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单管理", description = "订单相关接口")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderCreateDTO orderDTO,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OrderVO orderVO = orderService.createOrder(userId, orderDTO);
        return Result.success(orderVO);
    }
    
    @Operation(summary = "获取订单详情")
    @GetMapping("/detail/{orderId}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OrderVO orderVO = orderService.getOrderDetail(userId, orderId);
        return Result.success(orderVO);
    }
    
    @Operation(summary = "获取订单列表")
    @GetMapping("/list")
    public Result<List<OrderVO>> getOrderList(
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<OrderVO> list = orderService.getOrderList(userId, status);
        return Result.success(list);
    }
    
    @Operation(summary = "取消订单")
    @PutMapping("/cancel/{orderId}")
    public Result<Void> cancelOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.cancelOrder(userId, orderId);
        return Result.success();
    }
    
    @Operation(summary = "支付订单")
    @PutMapping("/pay/{orderId}")
    public Result<Void> payOrder(@PathVariable Long orderId,
                                 @RequestParam Integer payType,
                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.payOrder(userId, orderId, payType);
        return Result.success();
    }
    
    @Operation(summary = "确认收货")
    @PutMapping("/confirm/{orderId}")
    public Result<Void> confirmReceive(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.confirmReceive(userId, orderId);
        return Result.success();
    }
    
    @Operation(summary = "申请退款")
    @PutMapping("/refund/{orderId}")
    public Result<Void> applyRefund(@PathVariable Long orderId,
                                    @RequestParam String reason,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.applyRefund(userId, orderId, reason);
        return Result.success();
    }
    
    @Operation(summary = "发货订单")
    @PutMapping("/ship/{orderId}")
    public Result<Void> shipOrder(@PathVariable Long orderId,
                                  HttpServletRequest request) {
        // 管理员操作，不需要检查 userId
        orderService.shipOrder(null, orderId);
        return Result.success();
    }
}
