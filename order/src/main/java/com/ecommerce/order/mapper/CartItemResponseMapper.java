package com.ecommerce.order.mapper;

import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.domain.CartItem;

public class CartItemResponseMapper {

    private CartItemResponseMapper() {
    }

    public static CartItemResponse toCartItemResponse(CartItem cartItem){
        return new CartItemResponse(cartItem.getId(),
                cartItem.getUserId(),
                cartItem.getProductId(),
                cartItem.getQuantity(),
                cartItem.getPrice());
    }
}
