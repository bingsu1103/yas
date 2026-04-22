package com.yas.inventory.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.exception.StockExistingException;
import com.yas.inventory.model.Stock;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.repository.StockRepository;
import com.yas.inventory.repository.WarehouseRepository;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.stock.StockPostVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StockServiceTest {

    private WarehouseRepository warehouseRepository;
    private StockRepository stockRepository;
    private ProductService productService;
    private WarehouseService warehouseService;
    private StockHistoryService stockHistoryService;
    private StockService stockService;

    @BeforeEach
    void setUp() {
        warehouseRepository = mock(WarehouseRepository.class);
        stockRepository = mock(StockRepository.class);
        productService = mock(ProductService.class);
        warehouseService = mock(WarehouseService.class);
        stockHistoryService = mock(StockHistoryService.class);

        stockService = new StockService(
                warehouseRepository,
                stockRepository,
                productService,
                warehouseService,
                stockHistoryService
        );
    }

    @Test
    void addProductIntoWarehouse_whenStockAlreadyExisted_thenThrowStockExistingException() {
        StockPostVm vm = new StockPostVm(1L, 1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 1L)).thenReturn(true);

        assertThrows(StockExistingException.class, () -> stockService.addProductIntoWarehouse(List.of(vm)));
    }

    @Test
    void addProductIntoWarehouse_whenProductNotFound_thenThrowNotFoundException() {
        StockPostVm vm = new StockPostVm(1L, 1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 1L)).thenReturn(false);
        when(productService.getProduct(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> stockService.addProductIntoWarehouse(List.of(vm)));
    }

    @Test
    void addProductIntoWarehouse_whenWarehouseNotFound_thenThrowNotFoundException() {
        StockPostVm vm = new StockPostVm(1L, 1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 1L)).thenReturn(false);
        when(productService.getProduct(1L)).thenReturn(mock(ProductInfoVm.class));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> stockService.addProductIntoWarehouse(List.of(vm)));
    }

    @Test
    void addProductIntoWarehouse_whenValid_thenSaveStock() {
        StockPostVm vm = new StockPostVm(1L, 1L);
        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 1L)).thenReturn(false);
        when(productService.getProduct(1L)).thenReturn(mock(ProductInfoVm.class));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        stockService.addProductIntoWarehouse(List.of(vm));

        verify(stockRepository).saveAll(any());
    }
}
