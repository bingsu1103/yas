package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeValue;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailInfoVm;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductDetailServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MediaService mediaService;

    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;

    @InjectMocks
    private ProductDetailService productDetailService;

    private Product product;
    private NoFileMediaVm mediaVm;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setSlug("test-product");
        product.setShortDescription("Short desc");
        product.setDescription("Description");
        product.setSpecification("Spec");
        product.setSku("SKU1");
        product.setGtin("GTIN1");
        product.setPrice(100.0);
        product.setPublished(true);
        product.setFeatured(false);
        product.setVisibleIndividually(true);
        product.setStockTrackingEnabled(true);
        product.setProductCategories(Collections.emptyList());
        product.setAttributeValues(Collections.emptyList());

        mediaVm = new NoFileMediaVm(1L, "caption", "file.jpg", "image/jpeg", "http://url");
    }

    @Test
    void getProductDetailById_whenProductNotFound_thenThrowNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productDetailService.getProductDetailById(1L));
    }

    @Test
    void getProductDetailById_whenProductNotPublished_thenThrowNotFoundException() {
        product.setPublished(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        assertThrows(NotFoundException.class, () -> productDetailService.getProductDetailById(1L));
    }

    @Test
    void getProductDetailById_whenProductExists_thenReturnDetail() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
    }

    @Test
    void getProductDetailById_whenProductHasBrand_thenReturnBrandInfo() {
        Brand brand = new Brand();
        brand.setId(10L);
        brand.setName("BrandX");
        product.setBrand(brand);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertEquals(10L, result.getBrandId());
        assertEquals("BrandX", result.getBrandName());
    }

    @Test
    void getProductDetailById_whenProductHasNoBrand_thenBrandFieldsNull() {
        product.setBrand(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertNull(result.getBrandId());
        assertNull(result.getBrandName());
    }

    @Test
    void getProductDetailById_whenProductHasCategories_thenReturnCategories() {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Electronics");
        ProductCategory pc = new ProductCategory();
        pc.setCategory(cat);
        pc.setProduct(product);
        product.setProductCategories(List.of(pc));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertEquals(1, result.getCategories().size());
    }

    @Test
    void getProductDetailById_whenProductHasThumbnail_thenReturnThumbnail() {
        product.setThumbnailMediaId(5L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(5L)).thenReturn(mediaVm);

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertNotNull(result.getThumbnail());
    }

    @Test
    void getProductDetailById_whenProductHasNoThumbnail_thenThumbnailNull() {
        product.setThumbnailMediaId(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertNull(result.getThumbnail());
    }

    @Test
    void getProductDetailById_whenProductHasImages_thenReturnImages() {
        ProductImage img = new ProductImage();
        img.setImageId(3L);
        product.setProductImages(List.of(img));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(3L)).thenReturn(mediaVm);

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertEquals(1, result.getProductImages().size());
    }

    @Test
    void getProductDetailById_whenProductHasAttributes_thenReturnAttributes() {
        ProductAttribute attr = new ProductAttribute();
        attr.setId(1L);
        attr.setName("Color");
        ProductAttributeValue attrVal = new ProductAttributeValue();
        attrVal.setProductAttribute(attr);
        attrVal.setValue("Red");
        attrVal.setId(1L);
        product.setAttributeValues(List.of(attrVal));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertEquals(1, result.getAttributeValues().size());
    }

    @Test
    void getProductDetailById_whenProductHasOptions_thenReturnVariations() {
        product.setHasOptions(true);

        Product variation = new Product();
        variation.setId(2L);
        variation.setName("Var1");
        variation.setSlug("var1");
        variation.setSku("SKU-VAR1");
        variation.setGtin("GTIN-VAR1");
        variation.setPrice(120.0);
        variation.setPublished(true);
        variation.setProductImages(Collections.emptyList());
        product.setProducts(List.of(variation));

        ProductOption option = new ProductOption();
        option.setId(1L);
        ProductOptionCombination combo = new ProductOptionCombination();
        combo.setProductOption(option);
        combo.setValue("Red");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(variation)).thenReturn(List.of(combo));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertEquals(1, result.getVariations().size());
    }

    @Test
    void getProductDetailById_whenVariationNotPublished_thenExcludeFromVariations() {
        product.setHasOptions(true);

        Product variation = new Product();
        variation.setId(2L);
        variation.setName("Var1");
        variation.setSlug("var1");
        variation.setPublished(false);
        variation.setProductImages(Collections.emptyList());
        product.setProducts(List.of(variation));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertTrue(result.getVariations().isEmpty());
    }

    @Test
    void getProductDetailById_whenProductHasNoOptions_thenVariationsEmpty() {
        product.setHasOptions(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertTrue(result.getVariations().isEmpty());
    }
}
