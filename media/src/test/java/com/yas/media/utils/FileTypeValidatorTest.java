package com.yas.media.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class FileTypeValidatorTest {

    private FileTypeValidator validator;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @BeforeEach
    void setUp() {
        validator = new FileTypeValidator();
        context = mock(ConstraintValidatorContext.class);
        builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        ValidFileType annotation = mock(ValidFileType.class);
        when(annotation.allowedTypes()).thenReturn(new String[]{"image/jpeg", "image/png"});
        when(annotation.message()).thenReturn("Invalid file type");
        validator.initialize(annotation);
    }

    @Test
    void isValid_whenFileIsNull_thenReturnFalse() {
        boolean result = validator.isValid(null, context);
        assertFalse(result);
    }

    @Test
    void isValid_whenContentTypeIsNull_thenReturnFalse() {
        MultipartFile file = new MockMultipartFile("file", "test.txt", null, new byte[0]);
        boolean result = validator.isValid(file, context);
        assertFalse(result);
    }

    @Test
    void isValid_whenContentTypeNotAllowed_thenReturnFalse() {
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        boolean result = validator.isValid(file, context);
        assertFalse(result);
    }

    @Test
    void isValid_whenValidJpegImage_thenReturnTrue() throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", imageBytes);
        boolean result = validator.isValid(file, context);
        assertTrue(result);
    }

    @Test
    void isValid_whenValidPngImage_thenReturnTrue() throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", imageBytes);
        boolean result = validator.isValid(file, context);
        assertTrue(result);
    }

    @Test
    void isValid_whenAllowedTypeButNotImage_thenReturnFalse() {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "not-an-image".getBytes());
        boolean result = validator.isValid(file, context);
        assertFalse(result);
    }
}
