package com.crud.system.dto;

import com.crud.system.model.Order;
import com.crud.system.model.OrderItem;
import com.crud.system.model.OrderStatus;
import com.crud.system.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para os Mappers e DTOs do pacote dto.
 */
@DisplayName("Mapper e DTO Tests")
class MapperTest {

    @Nested
    @DisplayName("ProductMapper")
    class ProductMapperTests {

        @Test
        @DisplayName("Deve converter ProductRequestDTO para entidade Product")
        void deveConverterDtoParaEntidade() {
            ProductRequestDTO dto = new ProductRequestDTO();
            dto.setName("Teclado Mecânico");
            dto.setDescription("Teclado mecânico RGB com switches blue");
            dto.setPrice(new BigDecimal("349.90"));
            dto.setQuantity(25);
            dto.setCategory("Periféricos");

            Product product = ProductMapper.toEntity(dto);

            assertEquals("Teclado Mecânico", product.getName());
            assertEquals("Teclado mecânico RGB com switches blue", product.getDescription());
            assertEquals(new BigDecimal("349.90"), product.getPrice());
            assertEquals(25, product.getQuantity());
            assertEquals("Periféricos", product.getCategory());
            assertNull(product.getId());
        }

        @Test
        @DisplayName("Deve converter entidade Product para ProductResponseDTO")
        void deveConverterEntidadeParaResponseDto() {
            LocalDateTime agora = LocalDateTime.now();
            Product product = Product.builder()
                    .id(5L)
                    .name("Monitor Ultrawide")
                    .description("Monitor 34 polegadas curvo ultrawide")
                    .price(new BigDecimal("2899.00"))
                    .quantity(8)
                    .category("Monitores")
                    .createdAt(agora)
                    .updatedAt(agora)
                    .build();

            ProductResponseDTO dto = ProductMapper.toResponseDTO(product);

            assertEquals(5L, dto.getId());
            assertEquals("Monitor Ultrawide", dto.getName());
            assertEquals("Monitor 34 polegadas curvo ultrawide", dto.getDescription());
            assertEquals(new BigDecimal("2899.00"), dto.getPrice());
            assertEquals(8, dto.getQuantity());
            assertEquals("Monitores", dto.getCategory());
            assertEquals(agora, dto.getCreatedAt());
            assertEquals(agora, dto.getUpdatedAt());
        }

        @Test
        @DisplayName("Deve atualizar entidade existente com dados do DTO")
        void deveAtualizarEntidadeComDadosDoDto() {
            Product product = Product.builder()
                    .id(1L)
                    .name("Nome Antigo")
                    .description("Descrição antiga do produto")
                    .price(new BigDecimal("100.00"))
                    .quantity(5)
                    .category("Velha")
                    .build();

            ProductRequestDTO dto = new ProductRequestDTO();
            dto.setName("Nome Novo");
            dto.setDescription("Descrição nova e atualizada do produto");
            dto.setPrice(new BigDecimal("199.90"));
            dto.setQuantity(15);
            dto.setCategory("Nova");

            ProductMapper.updateEntity(dto, product);

            assertEquals(1L, product.getId());
            assertEquals("Nome Novo", product.getName());
            assertEquals("Descrição nova e atualizada do produto", product.getDescription());
            assertEquals(new BigDecimal("199.90"), product.getPrice());
            assertEquals(15, product.getQuantity());
            assertEquals("Nova", product.getCategory());
        }
    }

    @Nested
    @DisplayName("OrderMapper")
    class OrderMapperTests {

        @Test
        @DisplayName("Deve converter Order com itens para OrderResponseDTO")
        void deveConverterOrderComItensParaDto() {
            LocalDateTime agora = LocalDateTime.now();

            Product produto1 = Product.builder().id(10L).name("Mouse Gamer").build();
            Product produto2 = Product.builder().id(20L).name("Mousepad XL").build();

            Order order = new Order();
            order.setId(1L);
            order.setClientName("João Silva");
            order.setClientEmail("joao@email.com");
            order.setStatus(OrderStatus.CONFIRMED);
            order.setTotalValue(new BigDecimal("259.80"));
            order.setCreatedAt(agora);
            order.setUpdatedAt(agora);

            OrderItem item1 = OrderItem.builder()
                    .id(100L)
                    .product(produto1)
                    .quantity(2)
                    .unitPrice(new BigDecimal("99.90"))
                    .subtotal(new BigDecimal("199.80"))
                    .build();

            OrderItem item2 = OrderItem.builder()
                    .id(101L)
                    .product(produto2)
                    .quantity(1)
                    .unitPrice(new BigDecimal("60.00"))
                    .subtotal(new BigDecimal("60.00"))
                    .build();

            order.setItems(new ArrayList<>(List.of(item1, item2)));

            OrderResponseDTO dto = OrderMapper.toResponseDTO(order);

            assertEquals(1L, dto.getId());
            assertEquals("João Silva", dto.getClientName());
            assertEquals("joao@email.com", dto.getClientEmail());
            assertEquals(OrderStatus.CONFIRMED, dto.getStatus());
            assertEquals(new BigDecimal("259.80"), dto.getTotalValue());
            assertEquals(agora, dto.getCreatedAt());
            assertEquals(agora, dto.getUpdatedAt());
            assertNotNull(dto.getItems());
            assertEquals(2, dto.getItems().size());

            OrderItemDTO itemDto1 = dto.getItems().get(0);
            assertEquals(100L, itemDto1.getId());
            assertEquals(10L, itemDto1.getProductId());
            assertEquals("Mouse Gamer", itemDto1.getProductName());
            assertEquals(2, itemDto1.getQuantity());
            assertEquals(new BigDecimal("99.90"), itemDto1.getUnitPrice());
            assertEquals(new BigDecimal("199.80"), itemDto1.getSubtotal());

            OrderItemDTO itemDto2 = dto.getItems().get(1);
            assertEquals(101L, itemDto2.getId());
            assertEquals(20L, itemDto2.getProductId());
            assertEquals("Mousepad XL", itemDto2.getProductName());
        }

        @Test
        @DisplayName("Deve retornar null quando Order for null")
        void deveRetornarNullQuandoOrderForNull() {
            OrderResponseDTO dto = OrderMapper.toResponseDTO(null);
            assertNull(dto);
        }

        @Test
        @DisplayName("Deve converter Order sem itens (items null)")
        void deveConverterOrderSemItens() {
            Order order = new Order();
            order.setId(2L);
            order.setClientName("Maria Souza");
            order.setClientEmail("maria@email.com");
            order.setStatus(OrderStatus.PENDING);
            order.setTotalValue(BigDecimal.ZERO);
            order.setItems(null);

            OrderResponseDTO dto = OrderMapper.toResponseDTO(order);

            assertEquals(2L, dto.getId());
            assertEquals("Maria Souza", dto.getClientName());
            assertNull(dto.getItems());
        }
    }

    @Nested
    @DisplayName("DTOs - Getters e Setters")
    class DtoGettersSettersTests {

        @Test
        @DisplayName("ProductRequestDTO deve armazenar e retornar todos os campos")
        void productRequestDtoGettersSetters() {
            ProductRequestDTO dto = new ProductRequestDTO(
                    "Cadeira Gamer",
                    "Cadeira ergonômica com apoio lombar",
                    new BigDecimal("1200.00"),
                    12,
                    "Móveis"
            );

            assertEquals("Cadeira Gamer", dto.getName());
            assertEquals("Cadeira ergonômica com apoio lombar", dto.getDescription());
            assertEquals(new BigDecimal("1200.00"), dto.getPrice());
            assertEquals(12, dto.getQuantity());
            assertEquals("Móveis", dto.getCategory());

            dto.setName("Cadeira Office");
            assertEquals("Cadeira Office", dto.getName());
        }

        @Test
        @DisplayName("ProductResponseDTO deve armazenar e retornar todos os campos")
        void productResponseDtoGettersSetters() {
            LocalDateTime agora = LocalDateTime.now();
            ProductResponseDTO dto = new ProductResponseDTO(
                    7L, "SSD NVMe", "SSD 1TB NVMe Gen4", new BigDecimal("499.90"),
                    30, "Armazenamento", agora, agora
            );

            assertEquals(7L, dto.getId());
            assertEquals("SSD NVMe", dto.getName());
            assertEquals("SSD 1TB NVMe Gen4", dto.getDescription());
            assertEquals(new BigDecimal("499.90"), dto.getPrice());
            assertEquals(30, dto.getQuantity());
            assertEquals("Armazenamento", dto.getCategory());
            assertEquals(agora, dto.getCreatedAt());
            assertEquals(agora, dto.getUpdatedAt());
        }

        @Test
        @DisplayName("ProductResponseDTO deve funcionar com construtor vazio")
        void productResponseDtoConstrutorVazio() {
            ProductResponseDTO dto = new ProductResponseDTO();
            dto.setId(3L);
            dto.setName("Webcam HD");
            dto.setDescription("Webcam Full HD 1080p");
            dto.setPrice(new BigDecimal("189.00"));
            dto.setQuantity(50);
            dto.setCategory("Periféricos");
            dto.setCreatedAt(LocalDateTime.now());
            dto.setUpdatedAt(LocalDateTime.now());

            assertEquals(3L, dto.getId());
            assertEquals("Webcam HD", dto.getName());
        }

        @Test
        @DisplayName("CreateOrderRequestDTO deve armazenar e retornar todos os campos")
        void createOrderRequestDtoGettersSetters() {
            OrderItemDTO itemDto = new OrderItemDTO(1L, 3, null, null, null, null);
            List<OrderItemDTO> items = List.of(itemDto);

            CreateOrderRequestDTO dto = new CreateOrderRequestDTO(
                    "Carlos Pereira", "carlos@email.com", items
            );

            assertEquals("Carlos Pereira", dto.getClientName());
            assertEquals("carlos@email.com", dto.getClientEmail());
            assertEquals(1, dto.getItems().size());
            assertEquals(1L, dto.getItems().get(0).getProductId());

            dto.setClientName("Ana Costa");
            assertEquals("Ana Costa", dto.getClientName());
        }

        @Test
        @DisplayName("CreateOrderRequestDTO deve funcionar com construtor vazio")
        void createOrderRequestDtoConstrutorVazio() {
            CreateOrderRequestDTO dto = new CreateOrderRequestDTO();
            dto.setClientName("Pedro");
            dto.setClientEmail("pedro@email.com");
            dto.setItems(new ArrayList<>());

            assertEquals("Pedro", dto.getClientName());
            assertEquals("pedro@email.com", dto.getClientEmail());
            assertTrue(dto.getItems().isEmpty());
        }

        @Test
        @DisplayName("OrderItemDTO deve armazenar e retornar todos os campos")
        void orderItemDtoGettersSetters() {
            OrderItemDTO dto = new OrderItemDTO(
                    5L, 2, 50L, "Headset Bluetooth",
                    new BigDecimal("179.90"), new BigDecimal("359.80")
            );

            assertEquals(5L, dto.getProductId());
            assertEquals(2, dto.getQuantity());
            assertEquals(50L, dto.getId());
            assertEquals("Headset Bluetooth", dto.getProductName());
            assertEquals(new BigDecimal("179.90"), dto.getUnitPrice());
            assertEquals(new BigDecimal("359.80"), dto.getSubtotal());
        }

        @Test
        @DisplayName("OrderItemDTO deve funcionar com construtor vazio")
        void orderItemDtoConstrutorVazio() {
            OrderItemDTO dto = new OrderItemDTO();
            dto.setProductId(8L);
            dto.setQuantity(4);
            dto.setId(99L);
            dto.setProductName("Cabo HDMI");
            dto.setUnitPrice(new BigDecimal("29.90"));
            dto.setSubtotal(new BigDecimal("119.60"));

            assertEquals(8L, dto.getProductId());
            assertEquals(4, dto.getQuantity());
            assertEquals(99L, dto.getId());
            assertEquals("Cabo HDMI", dto.getProductName());
            assertEquals(new BigDecimal("29.90"), dto.getUnitPrice());
            assertEquals(new BigDecimal("119.60"), dto.getSubtotal());
        }

        @Test
        @DisplayName("OrderResponseDTO deve armazenar e retornar todos os campos")
        void orderResponseDtoGettersSetters() {
            LocalDateTime agora = LocalDateTime.now();
            List<OrderItemDTO> items = List.of(new OrderItemDTO(1L, 1, null, null, null, null));

            OrderResponseDTO dto = new OrderResponseDTO(
                    10L, "Fernanda Lima", "fernanda@email.com",
                    OrderStatus.DELIVERED, items,
                    new BigDecimal("500.00"), agora, agora
            );

            assertEquals(10L, dto.getId());
            assertEquals("Fernanda Lima", dto.getClientName());
            assertEquals("fernanda@email.com", dto.getClientEmail());
            assertEquals(OrderStatus.DELIVERED, dto.getStatus());
            assertEquals(1, dto.getItems().size());
            assertEquals(new BigDecimal("500.00"), dto.getTotalValue());
            assertEquals(agora, dto.getCreatedAt());
            assertEquals(agora, dto.getUpdatedAt());
        }

        @Test
        @DisplayName("OrderResponseDTO deve funcionar com construtor vazio")
        void orderResponseDtoConstrutorVazio() {
            OrderResponseDTO dto = new OrderResponseDTO();
            dto.setId(20L);
            dto.setClientName("Bruno");
            dto.setClientEmail("bruno@email.com");
            dto.setStatus(OrderStatus.CANCELLED);
            dto.setItems(new ArrayList<>());
            dto.setTotalValue(BigDecimal.ZERO);
            dto.setCreatedAt(LocalDateTime.now());
            dto.setUpdatedAt(LocalDateTime.now());

            assertEquals(20L, dto.getId());
            assertEquals("Bruno", dto.getClientName());
            assertEquals(OrderStatus.CANCELLED, dto.getStatus());
            assertTrue(dto.getItems().isEmpty());
        }
    }
}
