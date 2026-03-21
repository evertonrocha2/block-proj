package com.crud.system.service;

import com.crud.system.exception.BusinessRuleException;
import com.crud.system.exception.ResourceNotFoundException;
import com.crud.system.model.Order;
import com.crud.system.model.OrderItem;
import com.crud.system.model.OrderStatus;
import com.crud.system.model.Product;
import com.crud.system.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Serviço de pedidos. Gerencia criação, status e integração
 * com o estoque de produtos (reduz ao criar, devolve ao cancelar).
 */
@Service
@Transactional
public class OrderService implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final IProductService productService;

    @Autowired
    public OrderService(OrderRepository orderRepository, IProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    /**
     * Cria um pedido validando e reduzindo o estoque dos produtos.
     * Se algum item não tiver estoque suficiente, lança exceção antes de persistir.
     */
    @Override
    public Order createOrder(Order order) {
        logger.info("Criando pedido para: {}", order.getClientName());

        try {
            validateAndReduceStock(order);
            order.calculateTotal();

            Order saved = orderRepository.save(order);
            logger.info("Pedido criado - ID: {}", saved.getId());
            return saved;

        } catch (DataAccessException e) {
            logger.error("Erro de banco ao criar pedido", e);
            throw new BusinessRuleException("Erro ao criar pedido. Por favor, tente novamente.", e);
        }
    }

    /** Retorna todos os pedidos. */
    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        try {
            return orderRepository.findAll();
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar pedidos", e);
            throw new BusinessRuleException("Erro ao buscar pedidos. Por favor, tente novamente.", e);
        }
    }

    /** Busca pedido por ID. */
    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID do pedido deve ser positivo");
        }

        try {
            return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar pedido ID: {}", id, e);
            throw new BusinessRuleException("Erro ao buscar pedido. Por favor, tente novamente.", e);
        }
    }

    /** Atualiza o status de um pedido. */
    @Override
    public Order updateOrderStatus(Long id, OrderStatus status) {
        logger.info("Atualizando status do pedido {} para {}", id, status);

        if (id <= 0) {
            throw new IllegalArgumentException("ID do pedido deve ser positivo");
        }

        try {
            Order order = getOrderById(id);
            order.setStatus(status);

            Order updated = orderRepository.save(order);
            logger.info("Status atualizado - pedido ID: {}", updated.getId());
            return updated;

        } catch (DataAccessException e) {
            logger.error("Erro ao atualizar pedido ID: {}", id, e);
            throw new BusinessRuleException("Erro ao atualizar pedido. Por favor, tente novamente.", e);
        }
    }

    /** Deleta um pedido. Se estava confirmado, devolve o estoque. */
    @Override
    public void deleteOrder(Long id) {
        logger.info("Deletando pedido ID: {}", id);

        if (id <= 0) {
            throw new IllegalArgumentException("ID do pedido deve ser positivo");
        }

        try {
            Order order = getOrderById(id);

            if (order.getStatus() == OrderStatus.CONFIRMED) {
                restoreStock(order);
            }

            orderRepository.delete(order);
            logger.info("Pedido deletado - ID: {}", id);

        } catch (DataAccessException e) {
            logger.error("Erro ao deletar pedido ID: {}", id, e);
            throw new BusinessRuleException("Erro ao deletar pedido. Por favor, tente novamente.", e);
        }
    }

    /** Filtra pedidos pelo status. */
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status não pode ser nulo");
        }

        try {
            return orderRepository.findByStatus(status);
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar pedidos por status: {}", status, e);
            throw new BusinessRuleException("Erro ao buscar pedidos por status. Por favor, tente novamente.", e);
        }
    }

    /** Busca pedidos de um cliente pelo email. */
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByClientEmail(String clientEmail) {
        if (clientEmail == null || clientEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email do cliente não pode ser vazio");
        }

        try {
            return orderRepository.findByClientEmail(clientEmail.trim());
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar pedidos por email: {}", clientEmail, e);
            throw new BusinessRuleException("Erro ao buscar pedidos por email. Por favor, tente novamente.", e);
        }
    }

    /** Confirma o pedido. */
    @Override
    public Order confirmOrder(Long id) {
        return updateOrderStatus(id, OrderStatus.CONFIRMED);
    }

    /** Marca como entregue. */
    @Override
    public Order deliverOrder(Long id) {
        return updateOrderStatus(id, OrderStatus.DELIVERED);
    }

    /**
     * Cancela o pedido e devolve o estoque dos produtos.
     * Não permite cancelar um pedido que já foi cancelado.
     */
    @Override
    public Order cancelOrder(Long id) {
        logger.info("Cancelando pedido ID: {}", id);

        if (id <= 0) {
            throw new IllegalArgumentException("ID do pedido deve ser positivo");
        }

        try {
            Order order = getOrderById(id);

            if (order.getStatus() == OrderStatus.CANCELLED) {
                throw new BusinessRuleException("Pedido já foi cancelado");
            }

            restoreStock(order);
            order.setStatus(OrderStatus.CANCELLED);

            Order cancelled = orderRepository.save(order);
            logger.info("Pedido cancelado - ID: {}", cancelled.getId());
            return cancelled;

        } catch (DataAccessException e) {
            logger.error("Erro ao cancelar pedido ID: {}", id, e);
            throw new BusinessRuleException("Erro ao cancelar pedido. Por favor, tente novamente.", e);
        }
    }

    /**
     * Valida estoque de todos os itens antes de salvar, e só depois reduz.
     * Faz em duas passagens pra garantir que nada seja reduzido se algum item falhar.
     */
    private void validateAndReduceStock(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BusinessRuleException("Pedido deve conter pelo menos um item");
        }

        // primeiro valida tudo
        for (OrderItem item : order.getItems()) {
            Product product = productService.getProductById(item.getProduct().getId());

            if (product.getQuantity() < item.getQuantity()) {
                throw new BusinessRuleException(
                    "Estoque insuficiente para o produto '" + product.getName() +
                    "'. Disponível: " + product.getQuantity() +
                    ", Solicitado: " + item.getQuantity()
                );
            }

            item.setProduct(product);
            item.setUnitPrice(product.getPrice());
        }

        // agora que ta tudo ok, reduz o estoque
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productService.updateProduct(product.getId(), product);
        }
    }

    /** Devolve o estoque dos produtos quando cancela um pedido. */
    private void restoreStock(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productService.updateProduct(product.getId(), product);
        }
    }
}
