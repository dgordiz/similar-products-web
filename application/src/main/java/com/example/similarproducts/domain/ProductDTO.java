package com.example.similarproducts.domain;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductDTO {

	private String id;
	private String name;
	private BigDecimal price;
	private Boolean availability;
}
