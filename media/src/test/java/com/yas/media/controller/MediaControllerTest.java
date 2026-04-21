package com.yas.media.controller;

import com.yas.media.exception.ControllerAdvisor;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import com.yas.media.model.Media;
import com.yas.media.viewmodel.MediaPostVm;
import com.yas.media.viewmodel.MediaVm;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;
import com.yas.media.model.dto.MediaDto;
import com.yas.media.service.MediaService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;

@WebMvcTest(excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@ContextConfiguration(classes = {
    MediaController.class,
    ControllerAdvisor.class
})
@AutoConfigureMockMvc(addFilters = false)
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaService mediaService;

    @Test
    void get_whenMediaFound_thenReturnOk() throws Exception {
        MediaVm mediaVm = new MediaVm(1L, "caption", "fileName", "image/jpeg", "http://url");
        when(mediaService.getMediaById(1L)).thenReturn(mediaVm);

        mockMvc.perform(get("/medias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.caption").value("caption"));
    }

    @Test
    void get_whenMediaNotFound_thenReturnNotFound() throws Exception {
        when(mediaService.getMediaById(1L)).thenReturn(null);

        mockMvc.perform(get("/medias/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_whenSuccess_thenReturnNoContent() throws Exception {
        doNothing().when(mediaService).removeMedia(1L);

        mockMvc.perform(delete("/medias/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getByIds_whenMediasFound_thenReturnOk() throws Exception {
        MediaVm mediaVm = new MediaVm(1L, "caption", "fileName", "image/jpeg", "http://url");
        when(mediaService.getMediaByIds(List.of(1L))).thenReturn(List.of(mediaVm));

        mockMvc.perform(get("/medias?ids=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getByIds_whenMediasNotFound_thenReturnNotFound() throws Exception {
        when(mediaService.getMediaByIds(any())).thenReturn(List.of());

        mockMvc.perform(get("/medias?ids=1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_whenValidRequest_thenReturnOk() throws Exception {
        Media media = new Media();
        media.setId(1L);
        media.setCaption("caption");
        media.setFileName("fileName");
        media.setMediaType("image/jpeg");

        when(mediaService.saveMedia(any(MediaPostVm.class))).thenReturn(media);

        // Create a valid tiny image
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        MockMultipartFile file = new MockMultipartFile("multipartFile", "test.jpg", "image/jpeg", imageBytes);

        mockMvc.perform(multipart("/medias")
                .file(file)
                .param("caption", "caption")
                .param("fileNameOverride", "fileName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getFile_whenSuccess_thenReturnInputStream() throws Exception {
        MediaDto mediaDto = MediaDto.builder()
                .content(new ByteArrayInputStream("content".getBytes()))
                .mediaType(org.springframework.http.MediaType.IMAGE_JPEG)
                .build();

        when(mediaService.getFile(anyLong(), anyString())).thenReturn(mediaDto);

        mockMvc.perform(get("/medias/1/file/test.jpg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.jpg\""))
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }
}
