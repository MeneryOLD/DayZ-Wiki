package com.dayzwiki.portal.service.user;

import com.dayzwiki.portal.dto.api.item.UserAvatar;
import com.dayzwiki.portal.repository.user.UserAvatarRepository;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class UserAvatarService {

    private final UserAvatarRepository userAvatarRepository;

    public String uploadAvatar(MultipartFile file, long userId) throws IOException {
        InputStream inputStream = file.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(inputStream)
                .size(512, 512)
                .keepAspectRatio(true)
                .outputFormat("png")
                .toOutputStream(outputStream);

        byte[] imageBytes = outputStream.toByteArray();

        UserAvatar userAvatar = userAvatarRepository.findByUserId(userId);
        if (userAvatar == null) {
            userAvatar = new UserAvatar();
            userAvatar.setUserId(userId);
        }
        userAvatar.setAvatar(imageBytes);
        userAvatarRepository.save(userAvatar);

        return "Avatar successfully uploaded!";
    }

    public byte[] getAvatar(long userId) {
        UserAvatar userAvatar = userAvatarRepository.findByUserId(userId);
        return userAvatar != null ? userAvatar.getAvatar() : null;
    }

}
