package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.model.ProductRelated;
import com.yas.product.model.attribute.ProductAttributeValue;
import com.yas.product.model.enumeration.FilterExistInWhSelection;
import com.yas.product.repository.BrandRepository;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.repository.ProductCategoryRepository;
import com.yas.product.repository.ProductImageRepository;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.repository.ProductOptionValueRepository;
import com.yas.product.repository.ProductRelatedRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailGetVm;
import com.yas.product.viewmodel.product.ProductDetailVm;
import com.yas.product.viewmodel.product.ProductEsDetailVm;
import com.yas.product.viewmodel.product.ProductFeatureGetVm;
import com.yas.product.viewmodel.product.ProductGetCheckoutListVm;
import com.yas.product.viewmodel.product.ProductInfoVm;
import com.yas.product.viewmodel.product.ProductListGetFromCategoryVm;
import com.yas.product.viewmodel.product.ProductListGetVm;
import com.yas.product.viewmodel.product.ProductListVm;
import com.yas.product.viewmodel.product.ProductPostVm;
import com.yas.product.viewmodel.product.ProductQuantityPostVm;
import com.yas.product.viewmodel.product.ProductQuantityPutVm;
import com.yas.product.viewmodel.product.ProductSlugGetVm;
import com.yas.product.viewmodel.product.ProductThumbnailGetVm;
import com.yas.product.viewmodel.product.ProductThumbnailVm;
import com.yas.product.viewmodel.product.ProductVariationGetVm;
import com.yas.product.viewmodel.product.ProductsGetVm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class ProductServiceTest {

    private ProductRepository productRepository;
    private BrandRepository brandRepository;
    private CategoryRepository categoryRepository;
    private MediaService mediaService;
    private ProductCategoryRepository productCategoryRepository;
    private ProductImageRepository productImageRepository;
    private ProductOptionRepository productOptionRepository;
    private ProductOptionValueRepository productOptionValueRepository;
    private ProductOptionCombinationRepository productOptionCombinationRepository;
    private ProductRelatedRepository productRelatedRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        brandRepository = Mockito.mock(BrandRepository.class);
        mediaService = Mockito.mock(MediaService.class);
        categoryRepository = Mockito.mock(CategoryRepository.class);
        productCategoryRepository = Mockito.mock(ProductCategoryRepository.class);
        productImageRepository = Mockito.mock(ProductImageRepository.class);
        productOptionRepository = Mockito.mock(ProductOptionRepository.class);
        productOptionValueRepository = Mockito.mock(ProductOptionValueRepository.class);
        productOptionCombinationRepository = Mockito.mock(ProductOptionCombinationRepository.class);
        productRelatedRepository = Mockito.mock(ProductRelatedRepository.class);

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

    // ========================== getProductById ==========================

    @Test
    void getProductById_whenProductNotFound_thenThrowNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductById(1L));
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
    void getProductById_whenProductHasBrand_thenReturnBrandId() {
        Product product = new Product();
        product.setId(1L);
        product.setSlug("slug");
        product.setName("Product 1");
        Brand brand = new Brand();
        brand.setId(10L);
        product.setBrand(brand);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailVm result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(10L, result.brandId());
    }

    @Test
    void getProductById_whenProductHasThumbnail_thenReturnThumbnailMedia() {
        Product product = new Product();
        product.setId(1L);
        product.setSlug("slug");
        product.setName("Product 1");
        product.setThumbnailMediaId(5L);
        NoFileMediaVm mediaVm = new NoFileMediaVm(5L, "caption", "file.jpg", "image/jpeg", "http://url");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(5L)).thenReturn(mediaVm);

        ProductDetailVm result = productService.getProductById(1L);

        assertNotNull(result);
        assertNotNull(result.thumbnailMedia());
    }

    @Test
    void getProductById_whenProductHasCategories_thenReturnCategories() {
        Product product = new Product();
        product.setId(1L);
        product.setSlug("slug");
        product.setName("Product 1");
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Electronics");
        ProductCategory pc = new ProductCategory();
        pc.setCategory(cat);
        pc.setProduct(product);
        product.setProductCategories(List.of(pc));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailVm result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1, result.categories().size());
    }

    @Test
    void getProductById_whenProductHasImages_thenReturnImages() {
        Product product = new Product();
        product.setId(1L);
        product.setSlug("slug");
        product.setName("Product 1");
        ProductImage img = new ProductImage();
        img.setImageId(3L);
        product.setProductImages(List.of(img));
        NoFileMediaVm mediaVm = new NoFileMediaVm(3L, "caption", "file.jpg", "image/jpeg", "http://url");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(3L)).thenReturn(mediaVm);

        ProductDetailVm result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1, result.productImageMedias().size());
    }

    @Test
    void getProductById_whenProductHasParent_thenReturnParentId() {
        Product parent = new Product();
        parent.setId(99L);
        Product product = new Product();
        product.setId(1L);
        product.setSlug("slug");
        product.setName("Product 1");
        product.setParent(parent);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailVm result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(99L, result.parentId());
    }

    // ========================== getProductsByBrand ==========================

    @Test
    void getProductsByBrand_whenBrandNotFound_thenThrowNotFoundException() {
        when(brandRepository.findBySlug(anyString())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductsByBrand("invalid-brand"));
    }

    @Test
    void getProductsByBrand_whenBrandExists_thenReturnProducts() {
        Brand brand = new Brand();
        brand.setId(1L);
        brand.setSlug("test-brand");
        Product product = new Product();
        product.setId(1L);
        product.setName("Prod");
        product.setSlug("prod");
        product.setThumbnailMediaId(1L);
        NoFileMediaVm mediaVm = new NoFileMediaVm(1L, "cap", "f.jpg", "image/jpeg", "http://url");
        when(brandRepository.findBySlug("test-brand")).thenReturn(Optional.of(brand));
        when(productRepository.findAllByBrandAndIsPublishedTrueOrderByIdAsc(brand)).thenReturn(List.of(product));
        when(mediaService.getMedia(1L)).thenReturn(mediaVm);

        List<ProductThumbnailVm> result = productService.getProductsByBrand("test-brand");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========================== getListFeaturedProducts ==========================

    @Test
    void getListFeaturedProducts_whenCalled_thenReturnPage() {
        Page<Product> page = new PageImpl<>(Collections.emptyList());
        when(productRepository.getFeaturedProduct(any(Pageable.class))).thenReturn(page);

        ProductFeatureGetVm result = productService.getListFeaturedProducts(0, 10);

        assertNotNull(result);
        assertNotNull(result.productList());
    }

    @Test
    void getListFeaturedProducts_whenHasProducts_thenReturnMappedProducts() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Featured");
        product.setSlug("featured");
        product.setThumbnailMediaId(1L);
        product.setPrice(99.0);
        NoFileMediaVm mediaVm = new NoFileMediaVm(1L, "cap", "f.jpg", "image/jpeg", "http://url");
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.getFeaturedProduct(any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(1L)).thenReturn(mediaVm);

        ProductFeatureGetVm result = productService.getListFeaturedProducts(0, 10);

        assertEquals(1, result.productList().size());
    }

    // ========================== getProductsWithFilter ==========================

    @Test
    void getProductsWithFilter_whenCalled_thenReturnList() {
        when(productRepository.getProductsWithFilter(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        ProductListGetVm result = productService.getProductsWithFilter(0, 10, "name", "brand");

        assertNotNull(result);
        assertNotNull(result.productContent());
    }

    @Test
    void getProductsWithFilter_whenProductsExist_thenReturnMappedProducts() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test");
        product.setSlug("test");
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.getProductsWithFilter(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(page);

        ProductListGetVm result = productService.getProductsWithFilter(0, 10, "Test", "Brand");

        assertEquals(1, result.productContent().size());
    }

    // ========================== deleteProduct ==========================

    @Test
    void deleteProduct_whenProductNotFound_thenThrowNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.deleteProduct(1L));
    }

    @Test
    void deleteProduct_whenProductExists_thenSetPublishedFalse() {
        Product product = new Product();
        product.setId(1L);
        product.setPublished(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        assertEquals(false, product.isPublished());
        Mockito.verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_whenProductIsVariation_thenDeleteOptionCombinations() {
        Product parent = new Product();
        parent.setId(99L);
        Product product = new Product();
        product.setId(1L);
        product.setPublished(true);
        product.setParent(parent);
        ProductOptionCombination combo = new ProductOptionCombination();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(product)).thenReturn(List.of(combo));

        productService.deleteProduct(1L);

        verify(productOptionCombinationRepository).deleteAll(List.of(combo));
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_whenProductIsVariationButNoCombinations_thenSkipDelete() {
        Product parent = new Product();
        parent.setId(99L);
        Product product = new Product();
        product.setId(1L);
        product.setPublished(true);
        product.setParent(parent);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(product)).thenReturn(Collections.emptyList());

        productService.deleteProduct(1L);

        verify(productOptionCombinationRepository, never()).deleteAll(anyList());
    }

    // ========================== getProductVariationsByParentId ==========================

    @Test
    void getProductVariationsByParentId_whenParentNotFound_thenThrowNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductVariationsByParentId(1L));
    }

    @Test
    void getProductVariationsByParentId_whenParentHasNoOptions_thenReturnEmptyList() {
        Product parent = new Product();
        parent.setId(1L);
        parent.setHasOptions(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(parent));

        List<ProductVariationGetVm> result = productService.getProductVariationsByParentId(1L);

        assertEquals(0, result.size());
    }

    @Test
    void getProductVariationsByParentId_whenParentHasOptionsAndVariations_thenReturnVariations() {
        Product parent = new Product();
        parent.setId(1L);
        parent.setHasOptions(true);

        Product variation = new Product();
        variation.setId(2L);
        variation.setName("Var1");
        variation.setSlug("var1");
        variation.setSku("SKU1");
        variation.setGtin("GTIN1");
        variation.setPrice(10.0);
        variation.setPublished(true);
        variation.setProductImages(Collections.emptyList());
        parent.setProducts(List.of(variation));

        ProductOption option = new ProductOption();
        option.setId(1L);
        ProductOptionCombination combo = new ProductOptionCombination();
        combo.setProductOption(option);
        combo.setValue("Red");

        when(productRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(productOptionCombinationRepository.findAllByProduct(variation)).thenReturn(List.of(combo));

        List<ProductVariationGetVm> result = productService.getProductVariationsByParentId(1L);

        assertEquals(1, result.size());
        assertEquals("Var1", result.get(0).name());
    }

    // ========================== updateProductQuantity ==========================

    @Test
    void updateProductQuantity_whenCalled_thenUpdateStock() {
        ProductQuantityPostVm vm = new ProductQuantityPostVm(1L, 10L);
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));

        productService.updateProductQuantity(List.of(vm));

        assertEquals(10L, product.getStockQuantity());
        Mockito.verify(productRepository).saveAll(any());
    }

    @Test
    void updateProductQuantity_whenMultipleProducts_thenUpdateAll() {
        ProductQuantityPostVm vm1 = new ProductQuantityPostVm(1L, 10L);
        ProductQuantityPostVm vm2 = new ProductQuantityPostVm(2L, 20L);
        Product product1 = new Product();
        product1.setId(1L);
        Product product2 = new Product();
        product2.setId(2L);
        when(productRepository.findAllByIdIn(any())).thenReturn(List.of(product1, product2));

        productService.updateProductQuantity(List.of(vm1, vm2));

        assertEquals(10L, product1.getStockQuantity());
        assertEquals(20L, product2.getStockQuantity());
    }

    // ========================== subtractStockQuantity ==========================

    @Test
    void subtractStockQuantity_whenTrackingEnabled_thenSubtract() {
        ProductQuantityPutVm item = new ProductQuantityPutVm(1L, 5L);
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(10L);
        product.setStockTrackingEnabled(true);
        when(productRepository.findAllByIdIn(any())).thenReturn(List.of(product));

        productService.subtractStockQuantity(List.of(item));

        assertEquals(5L, product.getStockQuantity());
    }

    @Test
    void subtractStockQuantity_whenTrackingDisabled_thenDoNotSubtract() {
        ProductQuantityPutVm item = new ProductQuantityPutVm(1L, 5L);
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(10L);
        product.setStockTrackingEnabled(false);
        when(productRepository.findAllByIdIn(any())).thenReturn(List.of(product));

        productService.subtractStockQuantity(List.of(item));

        assertEquals(10L, product.getStockQuantity());
    }

    @Test
    void subtractStockQuantity_whenSubtractMoreThanAvailable_thenSetToZero() {
        ProductQuantityPutVm item = new ProductQuantityPutVm(1L, 15L);
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(10L);
        product.setStockTrackingEnabled(true);
        when(productRepository.findAllByIdIn(any())).thenReturn(List.of(product));

        productService.subtractStockQuantity(List.of(item));

        assertEquals(0L, product.getStockQuantity());
    }

    // ========================== restoreStockQuantity ==========================

    @Test
    void restoreStockQuantity_whenTrackingEnabled_thenAdd() {
        ProductQuantityPutVm item = new ProductQuantityPutVm(1L, 5L);
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(10L);
        product.setStockTrackingEnabled(true);
        when(productRepository.findAllByIdIn(any())).thenReturn(List.of(product));

        productService.restoreStockQuantity(List.of(item));

        assertEquals(15L, product.getStockQuantity());
    }

    @Test
    void restoreStockQuantity_whenTrackingDisabled_thenDoNotRestore() {
        ProductQuantityPutVm item = new ProductQuantityPutVm(1L, 5L);
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(10L);
        product.setStockTrackingEnabled(false);
        when(productRepository.findAllByIdIn(any())).thenReturn(List.of(product));

        productService.restoreStockQuantity(List.of(item));

        assertEquals(10L, product.getStockQuantity());
    }

    // ========================== getProductsFromCategory ==========================

    @Test
    void getProductsFromCategory_whenCategoryNotFound_thenThrowNotFoundException() {
        when(categoryRepository.findBySlug(anyString())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductsFromCategory(0, 10, "invalid-category"));
    }

    @Test
    void getProductsFromCategory_whenCategoryExists_thenReturnProducts() {
        Category category = new Category();
        category.setId(1L);
        category.setSlug("electronics");

        Product product = new Product();
        product.setId(1L);
        product.setName("Phone");
        product.setSlug("phone");
        product.setThumbnailMediaId(1L);

        ProductCategory pc = new ProductCategory();
        pc.setProduct(product);
        pc.setCategory(category);

        Page<ProductCategory> page = new PageImpl<>(List.of(pc));
        NoFileMediaVm mediaVm = new NoFileMediaVm(1L, "cap", "f.jpg", "image/jpeg", "http://url");

        when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.of(category));
        when(productCategoryRepository.findAllByCategory(any(Pageable.class), any(Category.class))).thenReturn(page);
        when(mediaService.getMedia(1L)).thenReturn(mediaVm);

        ProductListGetFromCategoryVm result = productService.getProductsFromCategory(0, 10, "electronics");

        assertNotNull(result);
        assertEquals(1, result.productContent().size());
    }

    // ========================== createProduct ==========================

    @Test
    void createProduct_whenLengthLessThanWidth_thenThrowBadRequestException() {
        ProductPostVm vm = Mockito.mock(ProductPostVm.class);
        when(vm.length()).thenReturn(5.0);
        when(vm.width()).thenReturn(10.0);

        assertThrows(BadRequestException.class, () -> productService.createProduct(vm));
    }

    // ========================== getRelatedProductsBackoffice ==========================

    @Test
    void getRelatedProductsBackoffice_whenProductNotFound_thenThrowNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getRelatedProductsBackoffice(1L));
    }

    @Test
    void getRelatedProductsBackoffice_whenProductHasRelated_thenReturnRelatedProducts() {
        Product product = new Product();
        product.setId(1L);
        Product related = new Product();
        related.setId(2L);
        related.setName("Related");
        related.setSlug("related");
        related.setPrice(50.0);
        ProductRelated pr = new ProductRelated();
        pr.setProduct(product);
        pr.setRelatedProduct(related);
        product.setRelatedProducts(List.of(pr));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        List<ProductListVm> result = productService.getRelatedProductsBackoffice(1L);

        assertEquals(1, result.size());
    }

    // ========================== getProductSlug ==========================

    @Test
    void getProductSlug_whenProductNotFound_thenThrowNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductSlug(1L));
    }

    @Test
    void getProductSlug_whenProductIsParent_thenReturnSlugWithNullParentId() {
        Product product = new Product();
        product.setId(1L);
        product.setSlug("parent-slug");
        product.setParent(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductSlugGetVm result = productService.getProductSlug(1L);

        assertEquals("parent-slug", result.slug());
        assertNull(result.productVariantId());
    }

    @Test
    void getProductSlug_whenProductIsChild_thenReturnParentSlugWithProductId() {
        Product parent = new Product();
        parent.setId(99L);
        parent.setSlug("parent-slug");
        Product product = new Product();
        product.setId(1L);
        product.setSlug("child-slug");
        product.setParent(parent);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductSlugGetVm result = productService.getProductSlug(1L);

        assertEquals("parent-slug", result.slug());
        assertEquals(1L, result.productVariantId());
    }

    // ========================== getLatestProducts ==========================

    @Test
    void getLatestProducts_whenCountIsZero_thenReturnEmptyList() {
        List<ProductListVm> result = productService.getLatestProducts(0);
        assertTrue(result.isEmpty());
    }

    @Test
    void getLatestProducts_whenCountIsNegative_thenReturnEmptyList() {
        List<ProductListVm> result = productService.getLatestProducts(-1);
        assertTrue(result.isEmpty());
    }

    @Test
    void getLatestProducts_whenCountIsPositive_thenReturnProducts() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Latest");
        product.setSlug("latest");
        when(productRepository.getLatestProducts(any(Pageable.class))).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getLatestProducts(5);

        assertEquals(1, result.size());
    }

    @Test
    void getLatestProducts_whenNoProducts_thenReturnEmptyList() {
        when(productRepository.getLatestProducts(any(Pageable.class))).thenReturn(Collections.emptyList());

        List<ProductListVm> result = productService.getLatestProducts(5);

        assertTrue(result.isEmpty());
    }

    // ========================== getProductEsDetailById ==========================

    @Test
    void getProductEsDetailById_whenProductNotFound_thenThrowNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductEsDetailById(1L));
    }

    @Test
    void getProductEsDetailById_whenProductExists_thenReturnDetail() {
        Product product = new Product();
        product.setId(1L);
        product.setName("ES Product");
        product.setSlug("es-product");
        product.setPrice(100.0);
        product.setPublished(true);
        product.setVisibleIndividually(true);
        product.setProductCategories(Collections.emptyList());
        product.setAttributeValues(Collections.emptyList());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductEsDetailVm result = productService.getProductEsDetailById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("ES Product", result.name());
    }

    @Test
    void getProductEsDetailById_whenProductHasBrand_thenReturnBrandName() {
        Product product = new Product();
        product.setId(1L);
        product.setName("With Brand");
        product.setSlug("with-brand");
        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("BrandX");
        product.setBrand(brand);
        product.setProductCategories(Collections.emptyList());
        product.setAttributeValues(Collections.emptyList());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductEsDetailVm result = productService.getProductEsDetailById(1L);

        assertEquals("BrandX", result.brand());
    }

    @Test
    void getProductEsDetailById_whenProductHasThumbnail_thenReturnThumbnailId() {
        Product product = new Product();
        product.setId(1L);
        product.setName("With Thumb");
        product.setSlug("with-thumb");
        product.setThumbnailMediaId(5L);
        product.setProductCategories(Collections.emptyList());
        product.setAttributeValues(Collections.emptyList());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductEsDetailVm result = productService.getProductEsDetailById(1L);

        assertEquals(5L, result.thumbnailMediaId());
    }

    // ========================== getProductsByMultiQuery ==========================

    @Test
    void getProductsByMultiQuery_whenCalled_thenReturnProducts() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Multi");
        product.setSlug("multi");
        product.setThumbnailMediaId(1L);
        product.setPrice(50.0);
        Page<Product> page = new PageImpl<>(List.of(product));
        NoFileMediaVm mediaVm = new NoFileMediaVm(1L, "cap", "f.jpg", "image/jpeg", "http://url");

        when(productRepository.findByProductNameAndCategorySlugAndPriceBetween(
                anyString(), anyString(), any(), any(), any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(1L)).thenReturn(mediaVm);

        ProductsGetVm result = productService.getProductsByMultiQuery(0, 10, "Multi", "cat", 0.0, 100.0);

        assertNotNull(result);
        assertEquals(1, result.productContent().size());
    }

    // ========================== getProductByIds ==========================

    @Test
    void getProductByIds_whenCalled_thenReturnProducts() {
        Product product = new Product();
        product.setId(1L);
        product.setName("P1");
        product.setSlug("p1");
        when(productRepository.findAllByIdIn(any())).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getProductByIds(List.of(1L));

        assertEquals(1, result.size());
    }

    // ========================== getProductByCategoryIds ==========================

    @Test
    void getProductByCategoryIds_whenCalled_thenReturnProducts() {
        Product product = new Product();
        product.setId(1L);
        product.setName("ByCat");
        product.setSlug("bycat");
        when(productRepository.findByCategoryIdsIn(any())).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getProductByCategoryIds(List.of(1L));

        assertEquals(1, result.size());
    }

    // ========================== getProductByBrandIds ==========================

    @Test
    void getProductByBrandIds_whenCalled_thenReturnProducts() {
        Product product = new Product();
        product.setId(1L);
        product.setName("ByBrand");
        product.setSlug("bybrand");
        when(productRepository.findByBrandIdsIn(any())).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getProductByBrandIds(List.of(1L));

        assertEquals(1, result.size());
    }

    // ========================== getFeaturedProductsById ==========================

    @Test
    void getFeaturedProductsById_whenProductsExist_thenReturnThumbnails() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Featured");
        product.setSlug("featured");
        product.setThumbnailMediaId(1L);
        product.setPrice(50.0);
        NoFileMediaVm mediaVm = new NoFileMediaVm(1L, "cap", "f.jpg", "image/jpeg", "http://url");
        when(productRepository.findAllByIdIn(any())).thenReturn(List.of(product));
        when(mediaService.getMedia(1L)).thenReturn(mediaVm);

        List<ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(1L));

        assertEquals(1, result.size());
        assertEquals("Featured", result.get(0).name());
    }

    // ========================== getRelatedProductsStorefront ==========================

    @Test
    void getRelatedProductsStorefront_whenProductNotFound_thenThrowNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getRelatedProductsStorefront(1L, 0, 10));
    }

    @Test
    void getRelatedProductsStorefront_whenProductExists_thenReturnRelated() {
        Product product = new Product();
        product.setId(1L);
        Product relatedProduct = new Product();
        relatedProduct.setId(2L);
        relatedProduct.setName("Related");
        relatedProduct.setSlug("related");
        relatedProduct.setThumbnailMediaId(1L);
        relatedProduct.setPrice(50.0);
        relatedProduct.setPublished(true);
        ProductRelated pr = new ProductRelated();
        pr.setProduct(product);
        pr.setRelatedProduct(relatedProduct);
        Page<ProductRelated> page = new PageImpl<>(List.of(pr));
        NoFileMediaVm mediaVm = new NoFileMediaVm(1L, "cap", "f.jpg", "image/jpeg", "http://url");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRelatedRepository.findAllByProduct(any(), any())).thenReturn(page);
        when(mediaService.getMedia(1L)).thenReturn(mediaVm);

        ProductsGetVm result = productService.getRelatedProductsStorefront(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.productContent().size());
    }

    // ========================== getProductsForWarehouse ==========================

    @Test
    void getProductsForWarehouse_whenCalled_thenReturnProducts() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Warehouse");
        product.setSlug("warehouse");
        when(productRepository.findProductForWarehouse(anyString(), anyString(), any(), anyString()))
                .thenReturn(List.of(product));

        List<ProductInfoVm> result = productService.getProductsForWarehouse(
                "name", "sku", List.of(1L), FilterExistInWhSelection.ALL);

        assertEquals(1, result.size());
    }

    // ========================== setProductImages ==========================

    @Test
    void setProductImages_whenImageIdsEmpty_thenDeleteExistingAndReturnEmpty() {
        Product product = new Product();
        product.setId(1L);

        List<ProductImage> result = productService.setProductImages(Collections.emptyList(), product);

        assertTrue(result.isEmpty());
        verify(productImageRepository).deleteByProductId(1L);
    }

    @Test
    void setProductImages_whenImageIdsNull_thenDeleteExistingAndReturnEmpty() {
        Product product = new Product();
        product.setId(1L);

        List<ProductImage> result = productService.setProductImages(null, product);

        assertTrue(result.isEmpty());
        verify(productImageRepository).deleteByProductId(1L);
    }

    @Test
    void setProductImages_whenProductHasNoExistingImages_thenCreateNew() {
        Product product = new Product();
        product.setId(1L);
        product.setProductImages(null);

        List<ProductImage> result = productService.setProductImages(List.of(1L, 2L), product);

        assertEquals(2, result.size());
    }

    // ========================== getProductCheckoutList ==========================

    @Test
    void getProductCheckoutList_whenCalled_thenReturnCheckoutList() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Checkout");
        product.setSlug("checkout");
        product.setThumbnailMediaId(1L);
        product.setPrice(50.0);
        Page<Product> page = new PageImpl<>(List.of(product));
        NoFileMediaVm mediaVm = new NoFileMediaVm(1L, "cap", "f.jpg", "image/jpeg", "http://url");

        when(productRepository.findAllPublishedProductsByIds(any(), any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(1L)).thenReturn(mediaVm);

        ProductGetCheckoutListVm result = productService.getProductCheckoutList(0, 10, List.of(1L));

        assertNotNull(result);
    }

    // ========================== exportProducts ==========================

    @Test
    void exportProducts_whenCalled_thenReturnExportingDetails() {
        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("BrandX");
        Product product = new Product();
        product.setId(1L);
        product.setName("Export");
        product.setSlug("export");
        product.setBrand(brand);
        when(productRepository.getExportingProducts(anyString(), anyString())).thenReturn(List.of(product));

        var result = productService.exportProducts("name", "brand");

        assertEquals(1, result.size());
    }

    // ========================== getProductDetail ==========================

    @Test
    void getProductDetail_whenSlugNotFound_thenThrowNotFoundException() {
        when(productRepository.findBySlugAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductDetail("invalid"));
    }

    @Test
    void getProductDetail_whenSlugExists_thenReturnDetail() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Detail");
        product.setSlug("detail");
        product.setThumbnailMediaId(1L);
        product.setProductCategories(Collections.emptyList());
        product.setAttributeValues(Collections.emptyList());
        NoFileMediaVm mediaVm = new NoFileMediaVm(1L, "cap", "f.jpg", "image/jpeg", "http://url");

        when(productRepository.findBySlugAndIsPublishedTrue("detail")).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(mediaVm);

        ProductDetailGetVm result = productService.getProductDetail("detail");

        assertNotNull(result);
        assertEquals(1L, result.id());
    }
}
