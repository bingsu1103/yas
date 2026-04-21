package com.yas.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.media.config.YasConfig;
import com.yas.media.mapper.MediaVmMapper;
import com.yas.media.model.Media;
import com.yas.media.model.dto.MediaDto;
import com.yas.media.repository.FileSystemRepository;
import com.yas.media.repository.MediaRepository;
import com.yas.media.service.MediaServiceImpl;
import com.yas.media.viewmodel.MediaPostVm;
import com.yas.media.viewmodel.MediaVm;
import com.yas.media.viewmodel.NoFileMediaVm;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class MediaServiceUnitTest {

    @Spy
    private MediaVmMapper mediaVmMapper = Mappers.getMapper(MediaVmMapper.class);

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private FileSystemRepository fileSystemRepository;

    @Mock
    private YasConfig yasConfig;

    @InjectMocks
    private MediaServiceImpl mediaService;

    private Media media;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        media = new Media();
        media.setId(1L);
        media.setCaption("test");
        media.setFileName("file");
        media.setMediaType("image/jpeg");
    }

    @Test
    void getMedia_whenValidId_thenReturnData() {
        NoFileMediaVm noFileMediaVm = new NoFileMediaVm(1L, "Test", "fileName", "image/png");
        when(mediaRepository.findByIdWithoutFileInReturn(1L)).thenReturn(noFileMediaVm);
        when(yasConfig.publicUrl()).thenReturn("http://localhost:8080");

        MediaVm mediaVm = mediaService.getMediaById(1L);
        assertNotNull(mediaVm);
        assertEquals("Test", mediaVm.caption());
        assertEquals("fileName", mediaVm.fileName());
        assertEquals("image/png", mediaVm.mediaType());
    }

    @Test
    void getMedia_whenMediaNotFound_thenReturnNull() {
        when(mediaRepository.findByIdWithoutFileInReturn(1L)).thenReturn(null);

        MediaVm mediaVm = mediaService.getMediaById(1L);
        assertNull(mediaVm);
    }

    @Test
    void getFile_whenSuccess_thenReturnMediaDto() {
        media.setFilePath("path/to/file");
        when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        when(fileSystemRepository.getFile("path/to/file")).thenReturn(inputStream);

        MediaDto result = mediaService.getFile(1L, "file");

        assertNotNull(result);
        assertEquals(org.springframework.http.MediaType.IMAGE_JPEG, result.getMediaType());
        assertNotNull(result.getContent());
    }

    @Test
    void getFile_whenMediaNotFound_thenReturnEmptyDto() {
        when(mediaRepository.findById(1L)).thenReturn(Optional.empty());

        MediaDto result = mediaService.getFile(1L, "file");

        assertNotNull(result);
        assertNull(result.getContent());
    }

    @Test
    void getFile_whenFileNameNotMatch_thenReturnEmptyDto() {
        when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));

        MediaDto result = mediaService.getFile(1L, "wrong-name");

        assertNotNull(result);
        assertNull(result.getContent());
    }

    @Test
    void removeMedia_whenMediaNotFound_thenThrowsNotFoundException() {
        when(mediaRepository.findByIdWithoutFileInReturn(1L)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> mediaService.removeMedia(1L));
        assertEquals(String.format("Media %s is not found", 1L), exception.getMessage());
    }

    @Test
    void removeMedia_whenValidId_thenRemoveSuccess() {
        NoFileMediaVm noFileMediaVm = new NoFileMediaVm(1L, "Test", "fileName", "image/png");
        when(mediaRepository.findByIdWithoutFileInReturn(1L)).thenReturn(noFileMediaVm);
        doNothing().when(mediaRepository).deleteById(1L);

        mediaService.removeMedia(1L);

        verify(mediaRepository, times(1)).deleteById(1L);
    }

    @Test
    void saveMedia_whenValidPostVm_thenSaveSuccess() throws Exception {
        byte[] content = new byte[]{1, 2, 3};
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", content);
        MediaPostVm postVm = new MediaPostVm("caption", file, "fileName");

        when(fileSystemRepository.persistFile("fileName", content)).thenReturn("path/to/file");
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Media result = mediaService.saveMedia(postVm);

        assertNotNull(result);
        assertEquals("caption", result.getCaption());
        assertEquals("fileName", result.getFileName());
        assertEquals("image/jpeg", result.getMediaType());
        assertEquals("path/to/file", result.getFilePath());
    }

    @Test
    void getMediaByIds() {
        var ip15 = getMedia(1L, "Iphone 15");
        var macbook = getMedia(2L, "Macbook");
        var existingMedias = List.of(ip15, macbook);
        when(mediaRepository.findAllById(any())).thenReturn(existingMedias);
        when(yasConfig.publicUrl()).thenReturn("https://media/");

        var medias = mediaService.getMediaByIds(List.of(1L, 2L));

        assertFalse(medias.isEmpty());
        assertEquals(2, medias.size());
    }

    private static @NotNull Media getMedia(Long id, String name) {
        var media = new Media();
        media.setId(id);
        media.setFileName(name);
        return media;
    }
}
