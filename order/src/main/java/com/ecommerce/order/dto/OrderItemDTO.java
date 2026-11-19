package com.ecommerce.order.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderItemDTO {

    private Long id;
    private Long productId;
    private Integer quantity;
    @Schema(description = "Unit price")
    private BigDecimal price;
    @Schema(description = "Unit price x quantity")
    private BigDecimal subTotal;
}
