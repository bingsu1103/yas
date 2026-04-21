package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.repository.BrandRepository;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.repository.ProductCategoryRepository;
import com.yas.product.repository.ProductImageRepository;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.repository.ProductOptionValueRepository;
import com.yas.product.repository.ProductRelatedRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.ImageVm;
import com.yas.product.viewmodel.product.ProductDetailVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private MediaService mediaService;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductCategoryRepository productCategoryRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private ProductOptionRepository productOptionRepository;
    @Mock
    private ProductOptionValueRepository productOptionValueRepository;
    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;
    @Mock
    private ProductRelatedRepository productRelatedRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .slug("test-product")
                .price(100.0)
                .isPublished(true)
                .thumbnailMediaId(10L)
                .build();
    }

    @Test
    void getProductById_whenIdValid_thenReturnProductDetailVm() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(10L)).thenReturn(new ImageVm(10L, "http://media/10"));

        ProductDetailVm result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Test Product", result.name());
        assertEquals("test-product", result.slug());
    }

    @Test
    void getProductById_whenIdInvalid_thenThrowNotFoundException() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductById(1L));
    }

    @Test
    void getLatestProducts_whenCountValid_thenReturnList() {
        when(productRepository.getLatestProducts(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(product));

        var result = productService.getLatestProducts(1);

        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).name());
    }

    @Test
    void getLatestProducts_whenCountZero_thenReturnEmptyList() {
        var result = productService.getLatestProducts(0);
        assertEquals(0, result.size());
    }

    @Test
    void getProductsByBrand_whenBrandExists_thenReturnList() {
        Brand brand = Brand.builder().id(1L).name("Brand").slug("brand").build();
        when(brandRepository.findBySlug("brand")).thenReturn(Optional.of(brand));
        when(productRepository.findAllByBrandAndIsPublishedTrueOrderByIdAsc(brand)).thenReturn(List.of(product));
        when(mediaService.getMedia(10L)).thenReturn(new ImageVm(10L, "http://media/10"));

        var result = productService.getProductsByBrand("brand");

        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).name());
    }

    @Test
    void getProductsByBrand_whenBrandNotFound_thenThrowNotFoundException() {
        when(brandRepository.findBySlug(anyString())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductsByBrand("invalid"));
    }

    @Test
    void getFeaturedProducts_whenCalled_thenReturnList() {
        when(productRepository.findAllByIsPublishedTrueAndIsFeaturedTrueOrderByIdDesc(org.mockito.ArgumentMatchers.any())).thenReturn(org.springframework.data.domain.Page.empty());

        var result = productService.getFeaturedProducts(0, 10);

        assertNotNull(result);
        assertEquals(0, result.totalElements());
    }

    @Test
    void getProductsFromCategory_whenCategoryExists_thenReturnList() {
        Category category = Category.builder().id(1L).slug("cat").build();
        when(categoryRepository.findBySlug("cat")).thenReturn(Optional.of(category));
        when(productRepository.getProductsFromCategory(anyString(), anyString(), anyString(), anyString(), any())).thenReturn(org.springframework.data.domain.Page.empty());

        var result = productService.getProductsFromCategory("cat", 0, 10, "asc", "name");

        assertNotNull(result);
        assertEquals(0, result.totalElements());
    }

    @Test
    void getProductsFromCategory_whenCategoryNotFound_thenThrowNotFoundException() {
        when(categoryRepository.findBySlug(anyString())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductsFromCategory("invalid", 0, 10, "asc", "name"));
    }
}
