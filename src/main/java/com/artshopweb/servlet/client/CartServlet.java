package com.artshopweb.servlet.client;

import com.artshopweb.beans.Order;
import com.artshopweb.beans.OrderItem;
import com.artshopweb.dto.ErrorMessage;
import com.artshopweb.dto.OrderRequest;
import com.artshopweb.dto.SuccessMessage;
import com.artshopweb.service.CartService;
import com.artshopweb.service.OrderItemService;
import com.artshopweb.service.OrderService;
import com.artshopweb.utils.JsonUtils;
import com.artshopweb.utils.Protector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "CartServlet", value = "/cart")
public class CartServlet extends HttpServlet {
    private final OrderService orderService = new OrderService();
    private final OrderItemService orderItemService = new OrderItemService();
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/cartView.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy đối tượng orderRequest từ JSON trong request
        OrderRequest orderRequest = JsonUtils.get(request, OrderRequest.class);

        // Tạo order
        Order order = new Order(
                0L,
                orderRequest.getUserId(),
                1,
                orderRequest.getDeliveryMethod(),
                orderRequest.getDeliveryPrice(),
                LocalDateTime.now(),
                null
        );
        long orderId = Protector.of(() -> orderService.insert(order)).get(0L);

        String successMessage = "Đã đặt hàng và tạo đơn hàng thành công!";
        String errorMessage = "Đã có lỗi truy vấn!";

        Runnable doneFunction = () -> JsonUtils.out(
                response,
                new SuccessMessage(200, successMessage),
                HttpServletResponse.SC_OK);
        Runnable failFunction = () -> JsonUtils.out(
                response,
                new ErrorMessage(404, errorMessage),
                HttpServletResponse.SC_NOT_FOUND);

        if (orderId > 0L) {
            List<OrderItem> orderItems = orderRequest.getOrderItems().stream().map(orderItemRequest -> new OrderItem(
                    0L,
                    orderId,
                    orderItemRequest.getProductId(),
                    orderItemRequest.getPrice(),
                    orderItemRequest.getDiscount(),
                    orderItemRequest.getQuantity(),
                    LocalDateTime.now(),
                    null
            )).collect(Collectors.toList());

            Protector.of(() -> {
                        orderItemService.bulkInsert(orderItems);
                        cartService.delete(orderRequest.getCartId());
                    })
                    .done(r -> doneFunction.run())
                    .fail(e -> failFunction.run());
        } else {
            failFunction.run();
        }
    }
}
