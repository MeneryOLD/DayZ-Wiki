package com.dayzwiki.portal.service;

import com.dayzwiki.portal.dto.api.item.UserAvatar;
import com.dayzwiki.portal.repository.user.UserAvatarRepository;
import com.dayzwiki.portal.service.user.UserAvatarService;
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
class UserAvatarServiceTest {

    @Mock
    private UserAvatarRepository userAvatarRepository;

    @InjectMocks
    private UserAvatarService userAvatarService;

    private final long userId = 1L;

    @Test
    void testUploadAvatarSuccess() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream imageStream = classLoader.getResourceAsStream("img/test.jpg");
        assertNotNull(imageStream, "Image not found in resources");

        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(imageStream);

        UserAvatar existingAvatar = new UserAvatar();
        existingAvatar.setUserId(userId);
        when(userAvatarRepository.findByUserId(userId)).thenReturn(existingAvatar);

        userAvatarService.uploadAvatar(file, userId);
        verify(userAvatarRepository, times(1)).save(any(UserAvatar.class));
        verify(file, times(1)).getInputStream();
    }


    @Test
    void testGetAvatarExists() {
        byte[] avatarBytes = "avatar".getBytes();
        UserAvatar userAvatar = new UserAvatar();
        userAvatar.setUserId(userId);
        userAvatar.setAvatar(avatarBytes);

        when(userAvatarRepository.findByUserId(userId)).thenReturn(userAvatar);

        byte[] result = userAvatarService.getAvatar(userId);
        assertArrayEquals(avatarBytes, result);
    }

    @Test
    void testGetAvatarNotFound() {
        when(userAvatarRepository.findByUserId(userId)).thenReturn(null);

        byte[] result = userAvatarService.getAvatar(userId);
        assertNull(result);
    }
}