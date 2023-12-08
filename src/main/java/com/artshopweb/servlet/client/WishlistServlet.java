package com.artshopweb.servlet.client;

import com.artshopweb.beans.Product;
import com.artshopweb.beans.User;
import com.artshopweb.beans.WishlistItem;
import com.artshopweb.dto.ErrorMessage;
import com.artshopweb.dto.SuccessMessage;
import com.artshopweb.dto.WishlistItemRequest;
import com.artshopweb.service.ProductService;
import com.artshopweb.service.WishlistItemService;
import com.artshopweb.utils.JsonUtils;
import com.artshopweb.utils.Protector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "WishlistServlet", value = "/wishlist")
public class WishlistServlet extends HttpServlet {
    private final WishlistItemService wishlistItemService = new WishlistItemService();
    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");

        if (user != null) {
            List<WishlistItem> wishlistItems = Protector.of(() -> wishlistItemService.getByUserId(user.getId()))
                    .get(ArrayList::new);

            for (WishlistItem wishlistItem : wishlistItems) {
                wishlistItem.setProduct(productService.getById(wishlistItem.getProductId()).orElseGet(Product::new));
            }

            request.setAttribute("wishlistItems", wishlistItems);
        }

        request.getRequestDispatcher("/WEB-INF/views/wishlistView.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long id = Protector.of(() -> Long.parseLong(request.getParameter("id"))).get(0L);
        Protector.of(() -> wishlistItemService.delete(id));
        response.sendRedirect(request.getContextPath() + "/wishlist");
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WishlistItemRequest wishlistItemRequest = JsonUtils.get(request, WishlistItemRequest.class);

        String successMessage = "Đã thêm sản phẩm vào danh sách yêu thích thành công!";
        String errorMessage = "Đã có lỗi truy vấn!";

        Runnable doneFunction = () -> JsonUtils.out(
                response,
                new SuccessMessage(200, successMessage),
                HttpServletResponse.SC_OK);
        Runnable failFunction = () -> JsonUtils.out(
                response,
                new ErrorMessage(404, errorMessage),
                HttpServletResponse.SC_NOT_FOUND);

        WishlistItem wishlistItem = new WishlistItem();
        wishlistItem.setUserId(wishlistItemRequest.getUserId());
        wishlistItem.setProductId(wishlistItemRequest.getProductId());

        Protector.of(() -> wishlistItemService.insert(wishlistItem))
                .done(r -> doneFunction.run())
                .fail(e -> failFunction.run());
    }
}