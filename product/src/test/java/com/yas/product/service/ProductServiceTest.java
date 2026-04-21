package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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
import com.yas.product.model.Brand;
import com.yas.product.model.Product;
import com.yas.product.viewmodel.product.ProductDetailVm;
import com.yas.product.viewmodel.product.ProductFeatureGetVm;
import com.yas.product.viewmodel.product.ProductListGetVm;
import com.yas.product.viewmodel.product.ProductThumbnailGetVm;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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

    @Test
    void getProductById_whenProductExists_thenReturnProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setSlug("slug");
        product.setName("Product 1");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailVm result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void getListFeaturedProducts_whenCalled_thenReturnPage() {
        Page<Product> page = new PageImpl<>(Collections.emptyList());
        when(productRepository.getFeaturedProduct(any(Pageable.class))).thenReturn(page);

        ProductFeatureGetVm result = productService.getListFeaturedProducts(0, 10);

        assertNotNull(result);
        assertNotNull(result.productList());
    }

    @Test
    void getProductsWithFilter_whenCalled_thenReturnList() {
        when(productRepository.getProductsWithFilter(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        ProductListGetVm result = productService.getProductsWithFilter(0, 10, "name", "brand");

        assertNotNull(result);
        assertNotNull(result.productContent());
    }
}
