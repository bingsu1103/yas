package com.yas.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.order.mapper.OrderMapper;
import com.yas.order.model.Order;
import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentStatus;
import com.yas.order.repository.OrderItemRepository;
import com.yas.order.repository.OrderRepository;
import com.yas.order.viewmodel.order.OrderBriefVm;
import com.yas.order.viewmodel.order.OrderListVm;
import com.yas.order.viewmodel.order.OrderVm;
import com.yas.order.viewmodel.order.PaymentOrderStatusVm;
import com.yas.order.model.request.OrderRequest;
import com.yas.order.viewmodel.order.OrderPostVm;
import com.yas.order.viewmodel.orderaddress.OrderAddressPostVm;
import com.yas.order.viewmodel.order.OrderItemPostVm;
import com.yas.order.viewmodel.order.OrderExistsByProductAndUserGetVm;
import com.yas.order.viewmodel.order.OrderGetVm;
import com.yas.commonlibrary.utils.AuthenticationUtils;
import org.mockito.MockedStatic;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private ProductService productService;
    private CartService cartService;
    private OrderMapper orderMapper;
    private PromotionService promotionService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        productService = mock(ProductService.class);
        cartService = mock(CartService.class);
        orderMapper = mock(OrderMapper.class);
        promotionService = mock(PromotionService.class);

        orderService = new OrderService(
                orderRepository,
                orderItemRepository,
                productService,
                cartService,
                orderMapper,
                promotionService
        );
    }

    // ========================== getOrderWithItemsById ==========================

    @Nested
    class GetOrderWithItemsByIdTest {

        @Test
        void whenOrderNotFound_thenThrowNotFoundException() {
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> orderService.getOrderWithItemsById(1L));
        }

        @Test
        void whenOrderExists_thenReturnOrderVm() {
            Order order = new Order();
            order.setId(1L);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderItemRepository.findAllByOrderId(1L)).thenReturn(List.of());

            OrderVm result = orderService.getOrderWithItemsById(1L);

            assertNotNull(result);
            assertEquals(1L, result.id());
        }

        @Test
        void whenOrderHasItems_thenReturnOrderWithItems() {
            Order order = new Order();
            order.setId(1L);
            OrderItem item = new OrderItem();
            item.setProductId(10L);
            item.setQuantity(2);
            item.setProductName("Product");
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderItemRepository.findAllByOrderId(1L)).thenReturn(List.of(item));

            OrderVm result = orderService.getOrderWithItemsById(1L);

            assertNotNull(result);
            assertNotNull(result.orderItemVms());
        }
    }

    // ========================== acceptOrder ==========================

    @Nested
    class AcceptOrderTest {

        @Test
        void whenOrderNotFound_thenThrowNotFoundException() {
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> orderService.acceptOrder(1L));
        }

        @Test
        void whenOrderExists_thenUpdateStatusToAccepted() {
            Order order = new Order();
            order.setId(1L);
            order.setOrderStatus(OrderStatus.PENDING);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            orderService.acceptOrder(1L);

            assertEquals(OrderStatus.ACCEPTED, order.getOrderStatus());
            verify(orderRepository).save(order);
        }
    }

    // ========================== rejectOrder ==========================

    @Nested
    class RejectOrderTest {

        @Test
        void whenOrderNotFound_thenThrowNotFoundException() {
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> orderService.rejectOrder(1L, "reason"));
        }

        @Test
        void whenOrderExists_thenUpdateStatusToReject() {
            Order order = new Order();
            order.setId(1L);
            order.setOrderStatus(OrderStatus.PENDING);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            orderService.rejectOrder(1L, "reason");

            assertEquals(OrderStatus.REJECT, order.getOrderStatus());
            assertEquals("reason", order.getRejectReason());
            verify(orderRepository).save(order);
        }

        @Test
        void whenOrderExists_thenRejectReasonIsSet() {
            Order order = new Order();
            order.setId(1L);
            order.setOrderStatus(OrderStatus.ACCEPTED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            orderService.rejectOrder(1L, "out of stock");

            assertEquals("out of stock", order.getRejectReason());
        }
    }

    // ========================== getLatestOrders ==========================

    @Nested
    class GetLatestOrdersTest {

        @Test
        void whenCountIsZero_thenReturnEmptyList() {
            List<OrderBriefVm> result = orderService.getLatestOrders(0);
            assertEquals(0, result.size());
        }

        @Test
        void whenCountIsNegative_thenReturnEmptyList() {
            List<OrderBriefVm> result = orderService.getLatestOrders(-1);
            assertTrue(result.isEmpty());
        }

        @Test
        void whenCountIsPositive_thenReturnOrders() {
            Order order = new Order();
            order.setId(1L);
            order.setOrderStatus(OrderStatus.PENDING);
            when(orderRepository.getLatestOrders(any(Pageable.class))).thenReturn(List.of(order));

            List<OrderBriefVm> result = orderService.getLatestOrders(5);

            assertEquals(1, result.size());
        }

        @Test
        void whenNoOrders_thenReturnEmptyList() {
            when(orderRepository.getLatestOrders(any(Pageable.class))).thenReturn(Collections.emptyList());

            List<OrderBriefVm> result = orderService.getLatestOrders(5);

            assertTrue(result.isEmpty());
        }
    }

    // ========================== updateOrderPaymentStatus ==========================

    @Nested
    class UpdateOrderPaymentStatusTest {

        @Test
        void whenOrderNotFound_thenThrowNotFoundException() {
            PaymentOrderStatusVm vm = PaymentOrderStatusVm.builder()
                    .orderId(1L)
                    .paymentId(100L)
                    .paymentStatus("COMPLETED")
                    .build();
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> orderService.updateOrderPaymentStatus(vm));
        }

        @Test
        void whenPaymentCompleted_thenOrderStatusSetToPaid() {
            Order order = new Order();
            order.setId(1L);
            order.setOrderStatus(OrderStatus.PENDING);
            PaymentOrderStatusVm vm = PaymentOrderStatusVm.builder()
                    .orderId(1L)
                    .paymentId(100L)
                    .paymentStatus("COMPLETED")
                    .build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            PaymentOrderStatusVm result = orderService.updateOrderPaymentStatus(vm);

            assertEquals(OrderStatus.PAID, order.getOrderStatus());
            assertEquals(100L, order.getPaymentId());
            assertNotNull(result);
        }

        @Test
        void whenPaymentNotCompleted_thenOrderStatusNotChanged() {
            Order order = new Order();
            order.setId(1L);
            order.setOrderStatus(OrderStatus.PENDING);
            PaymentOrderStatusVm vm = PaymentOrderStatusVm.builder()
                    .orderId(1L)
                    .paymentId(100L)
                    .paymentStatus("PENDING")
                    .build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            PaymentOrderStatusVm result = orderService.updateOrderPaymentStatus(vm);

            assertEquals(OrderStatus.PENDING, order.getOrderStatus());
            assertEquals(PaymentStatus.PENDING, order.getPaymentStatus());
        }
    }

    // ========================== findOrderByCheckoutId ==========================

    @Nested
    class FindOrderByCheckoutIdTest {

        @Test
        void whenCheckoutIdNotFound_thenThrowNotFoundException() {
            when(orderRepository.findByCheckoutId(anyString())).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> orderService.findOrderByCheckoutId("invalid"));
        }

        @Test
        void whenCheckoutIdExists_thenReturnOrder() {
            Order order = new Order();
            order.setId(1L);
            when(orderRepository.findByCheckoutId("checkout-123")).thenReturn(Optional.of(order));

            Order result = orderService.findOrderByCheckoutId("checkout-123");

            assertNotNull(result);
            assertEquals(1L, result.getId());
        }
    }

    // ========================== findOrderVmByCheckoutId ==========================

    @Nested
    class FindOrderVmByCheckoutIdTest {

        @Test
        void whenCheckoutIdExists_thenReturnOrderGetVm() {
            Order order = new Order();
            order.setId(1L);
            when(orderRepository.findByCheckoutId("checkout-123")).thenReturn(Optional.of(order));
            when(orderItemRepository.findAllByOrderId(1L)).thenReturn(List.of());

            var result = orderService.findOrderVmByCheckoutId("checkout-123");

            assertNotNull(result);
        }

        @Test
        void whenCheckoutIdNotFound_thenThrowNotFoundException() {
            when(orderRepository.findByCheckoutId(anyString())).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> orderService.findOrderVmByCheckoutId("invalid"));
        }
    }

    // ========================== getAllOrder ==========================

    @Nested
    class GetAllOrderTest {

        @Test
        void whenOrdersExist_thenReturnOrderList() {
            Order order = new Order();
            order.setId(1L);
            order.setOrderStatus(OrderStatus.PENDING);
            Page<Order> page = new PageImpl<>(List.of(order));
            when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            OrderListVm result = orderService.getAllOrder(
                    Pair.of(ZonedDateTime.now().minusDays(1), ZonedDateTime.now()),
                    "",
                    List.of(),
                    Pair.of("", ""),
                    "",
                    Pair.of(0, 10)
            );

            assertNotNull(result);
            assertNotNull(result.orderList());
            assertEquals(1, result.orderList().size());
        }

        @Test
        void whenNoOrders_thenReturnEmptyOrderList() {
            Page<Order> emptyPage = new PageImpl<>(Collections.emptyList());
            when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

            OrderListVm result = orderService.getAllOrder(
                    Pair.of(ZonedDateTime.now().minusDays(1), ZonedDateTime.now()),
                    "",
                    List.of(),
                    Pair.of("", ""),
                    "",
                    Pair.of(0, 10)
            );

            assertNotNull(result);
            assertNull(result.orderList());
        }

        @Test
        void whenOrderStatusProvided_thenFilterByStatus() {
            Order order = new Order();
            order.setId(1L);
            order.setOrderStatus(OrderStatus.ACCEPTED);
            Page<Order> page = new PageImpl<>(List.of(order));
            when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            OrderListVm result = orderService.getAllOrder(
                    Pair.of(ZonedDateTime.now().minusDays(1), ZonedDateTime.now()),
                    "",
                    List.of(OrderStatus.ACCEPTED),
                    Pair.of("", ""),
                    "",
                    Pair.of(0, 10)
            );

            assertNotNull(result);
            assertEquals(1, result.orderList().size());
        }
    }

    // ========================== createOrder ==========================

    @Test
    void testCreateOrder_whenNormalCase_returnOrderVm() {
        // Arrange
        OrderAddressPostVm addressPostVm = OrderAddressPostVm.builder()
                .phone("123456789")
                .contactName("John Doe")
                .addressLine1("Address 1")
                .city("City")
                .build();

        OrderItemPostVm itemPostVm = OrderItemPostVm.builder()
                .productId(10L)
                .productName("Product")
                .quantity(2)
                .build();

        OrderPostVm orderPostVm = OrderPostVm.builder()
                .email("test@example.com")
                .billingAddressPostVm(addressPostVm)
                .shippingAddressPostVm(addressPostVm)
                .orderItemPostVms(List.of(itemPostVm))
                .checkoutId("checkout-123")
                .build();

        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus(OrderStatus.PENDING);

        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order)); // For acceptOrder internal call

        // Act
        OrderVm result = orderService.createOrder(orderPostVm);

        // Assert
        assertNotNull(result);
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(productService).subtractProductStockQuantity(any(OrderVm.class));
        verify(cartService).deleteCartItems(any(OrderVm.class));
    }

    // ========================== getMyOrders ==========================

    @Test
    void testGetMyOrders_whenOrdersExist_thenReturnOrderGetVmList() {
        try (MockedStatic<AuthenticationUtils> authMock = mockStatic(AuthenticationUtils.class)) {
            authMock.when(AuthenticationUtils::extractUserId).thenReturn("user-123");
            
            Order order = new Order();
            order.setId(1L);
            when(orderRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
                    .thenReturn(List.of(order));

            List<OrderGetVm> result = orderService.getMyOrders("product", OrderStatus.PENDING);

            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    // ========================== isOrderCompleted ==========================

    @Test
    void testIsOrderCompleted_whenOrderExists_thenReturnTrue() {
        try (MockedStatic<AuthenticationUtils> authMock = mockStatic(AuthenticationUtils.class)) {
            authMock.when(AuthenticationUtils::extractUserId).thenReturn("user-123");
            when(productService.getProductVariations(anyLong())).thenReturn(List.of());
            
            Order order = new Order();
            when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.of(order));

            OrderExistsByProductAndUserGetVm result = orderService.isOrderCompletedWithUserIdAndProductId(10L);

            assertTrue(result.isPresent());
        }
    }

    @Test
    void testIsOrderCompleted_whenOrderDoesNotExist_thenReturnFalse() {
        try (MockedStatic<AuthenticationUtils> authMock = mockStatic(AuthenticationUtils.class)) {
            authMock.when(AuthenticationUtils::extractUserId).thenReturn("user-123");
            when(productService.getProductVariations(anyLong())).thenReturn(List.of());
            when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

            OrderExistsByProductAndUserGetVm result = orderService.isOrderCompletedWithUserIdAndProductId(10L);

            assertFalse(result.isPresent());
        }
    }

    // ========================== exportCsv ==========================

    @Test
    void testExportCsv_whenNoOrders_thenReturnEmptyCsv() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setPageNo(0);
        request.setPageSize(10);
        
        Page<Order> emptyPage = new PageImpl<>(List.of());
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        byte[] result = orderService.exportCsv(request);

        assertNotNull(result);
    }
}
