package com.dayzwiki.portal.service.user;

import com.dayzwiki.portal.dto.api.item.UserBanner;
import com.dayzwiki.portal.repository.user.UserBannerRepository;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class UserBannerService {

    private final UserBannerRepository userBannerRepository;

    public void uploadBanner(MultipartFile file, long userId) throws IOException {
        InputStream inputStream = file.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(inputStream)
                .size(1920, 300)
                .outputQuality(1.0)
                .keepAspectRatio(true)
                .outputFormat("png")
                .toOutputStream(outputStream);

        byte[] imageBytes = outputStream.toByteArray();

        UserBanner userBanner = userBannerRepository.findByUserId(userId);
        if (userBanner == null) {
            userBanner = new UserBanner();
            userBanner.setUserId(userId);
        }
        userBanner.setBanner(imageBytes);
        userBannerRepository.save(userBanner);
    }

    public byte[] getBanner(long userId) {
        UserBanner userBanner = userBannerRepository.findByUserId(userId);
        return userBanner != null ? userBanner.getBanner() : null;
    }

}
