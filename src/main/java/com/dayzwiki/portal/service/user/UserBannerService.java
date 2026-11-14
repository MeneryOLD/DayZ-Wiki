package com.dayzwiki.portal.service.user;

import com.dayzwiki.portal.dto.api.item.UserBanner;
import com.dayzwiki.portal.repository.user.UserBannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class UserBannerService {

    private final UserBannerRepository userBannerRepository;

    public void uploadBanner(MultipartFile file, long userId) throws IOException {
        InputStream inputStream = file.getInputStream();
        // Never upscaled; big photos are reduced to fit, at high JPEG quality.
        byte[] imageBytes = ImageResizer.toJpeg(inputStream, 2400, 1350);

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
