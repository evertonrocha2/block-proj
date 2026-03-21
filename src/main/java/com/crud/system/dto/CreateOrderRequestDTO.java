package com.crud.system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequestDTO {

    @NotBlank(message = "Nome do cliente é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String clientName;

    @NotBlank(message = "Email do cliente é obrigatório")
    @Email(message = "Email deve ser válido")
    private String clientEmail;

    @NotEmpty(message = "Pedido deve conter pelo menos um item")
    private List<OrderItemDTO> items;
}
