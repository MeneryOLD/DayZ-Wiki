package com.dayzwiki.portal.service;

import com.dayzwiki.portal.dto.api.item.UserBanner;
import com.dayzwiki.portal.repository.user.UserBannerRepository;
import com.dayzwiki.portal.service.user.UserBannerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBannerServiceTest {

    @Mock
    private UserBannerRepository userBannerRepository;

    @InjectMocks
    private UserBannerService userBannerService;

    private final long userId = 1L;

    @Test
    void testUploadBannerSuccess() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream imageStream = classLoader.getResourceAsStream("img/test.jpg");
        assertNotNull(imageStream, "Image not found in resources");

        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(imageStream);

        UserBanner existingBanner = new UserBanner();
        existingBanner.setUserId(userId);
        when(userBannerRepository.findByUserId(userId)).thenReturn(existingBanner);

        userBannerService.uploadBanner(file, userId);

        verify(userBannerRepository, times(1)).save(any(UserBanner.class));
        verify(file, times(1)).getInputStream();
    }

    @Test
    void testGetBannerExists() {
        byte[] bannerBytes = "banner".getBytes();
        UserBanner userBanner = new UserBanner();
        userBanner.setUserId(userId);
        userBanner.setBanner(bannerBytes);

        when(userBannerRepository.findByUserId(userId)).thenReturn(userBanner);

        byte[] result = userBannerService.getBanner(userId);
        assertArrayEquals(bannerBytes, result);
    }

    @Test
    void testGetBannerNotFound() {
        when(userBannerRepository.findByUserId(userId)).thenReturn(null);

        byte[] result = userBannerService.getBanner(userId);
        assertNull(result);
    }
}
