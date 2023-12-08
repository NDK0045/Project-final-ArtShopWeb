package com.artshopweb.servlet.admin.order;

import com.artshopweb.beans.Order;
import com.artshopweb.beans.OrderItem;
import com.artshopweb.service.OrderItemService;
import com.artshopweb.service.OrderService;
import com.artshopweb.service.ProductService;
import com.artshopweb.service.UserService;
import com.artshopweb.utils.Protector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.artshopweb.servlet.admin.order.OrderManagerServlet.calculateTotalPrice;

@WebServlet(name = "OrderManagerDetailServlet", value = "/admin/orderManager/detail")
public class OrderManagerDetailServlet extends HttpServlet {
    private final OrderService orderService = new OrderService();
    private final UserService userService = new UserService();
    private final OrderItemService orderItemService = new OrderItemService();
    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long id = Protector.of(() -> Long.parseLong(request.getParameter("id"))).get(0L);
        Optional<Order> orderFromServer = Protector.of(() -> orderService.getById(id)).get(Optional::empty);

        if (orderFromServer.isPresent()) {
            Order order = orderFromServer.get();

            Protector.of(() -> userService.getById(order.getUserId())).get(Optional::empty).ifPresent(order::setUser);
            List<OrderItem> orderItems = Protector.of(() -> orderItemService.getByOrderId(order.getId())).get(ArrayList::new);
            orderItems.forEach(orderItem -> Protector.of(() -> productService.getById(orderItem.getProductId()))
                    .get(Optional.empty())
                    .ifPresent(orderItem::setProduct));
            order.setOrderItems(orderItems);
            order.setTotalPrice(calculateTotalPrice(orderItems, order.getDeliveryPrice()));

            request.setAttribute("order", order);
            request.getRequestDispatcher("/WEB-INF/views/orderManagerDetailView.jsp").forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/orderManager");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}
}
