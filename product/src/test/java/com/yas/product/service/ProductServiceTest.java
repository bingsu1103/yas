package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.repository.BrandRepository;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.repository.ProductCategoryRepository;
import com.yas.product.repository.ProductImageRepository;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.repository.ProductOptionValueRepository;
import com.yas.product.repository.ProductRelatedRepository;
import com.yas.product.repository.ProductRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ProductServiceTest {

    private ProductRepository productRepository;
    private BrandRepository brandRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        brandRepository = Mockito.mock(BrandRepository.class);
        var mediaService = Mockito.mock(MediaService.class);
        var categoryRepository = Mockito.mock(CategoryRepository.class);
        var productCategoryRepository = Mockito.mock(ProductCategoryRepository.class);
        var productImageRepository = Mockito.mock(ProductImageRepository.class);
        var productOptionRepository = Mockito.mock(ProductOptionRepository.class);
        var productOptionValueRepository = Mockito.mock(ProductOptionValueRepository.class);
        var productOptionCombinationRepository = Mockito.mock(ProductOptionCombinationRepository.class);
        var productRelatedRepository = Mockito.mock(ProductRelatedRepository.class);

        productService = new ProductService(
                productRepository,
                mediaService,
                brandRepository,
                productCategoryRepository,
                categoryRepository,
                productImageRepository,
                productOptionRepository,
                productOptionValueRepository,
                productOptionCombinationRepository,
                productRelatedRepository
        );
    }

    @Test
    void getProductById_whenProductNotFound_thenThrowNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductById(1L));
    }

    @Test
    void getProductsByBrand_whenBrandNotFound_thenThrowNotFoundException() {
        when(brandRepository.findBySlug(anyString())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductsByBrand("invalid-brand"));
    }
}
