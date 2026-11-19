package com.ecommerce.product.dto;

import lombok.Builder;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private int stock;
    private String image;
    private boolean active;
    @Schema(description = "Not populated yet: always null")
    private LocalDateTime createdAt;
    @Schema(description = "Not populated yet: always null")
    private LocalDateTime updatedAt;
}
