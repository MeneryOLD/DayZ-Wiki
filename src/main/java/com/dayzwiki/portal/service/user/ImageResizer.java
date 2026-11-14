package com.dayzwiki.portal.service.user;

import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Re-encodes uploaded profile pictures and covers as high-quality JPEG.
 * Images are only ever scaled down (never up), so a sharp upload stays sharp.
 */
public final class ImageResizer {

    /** Background for transparent PNGs (JPEG has no transparency); matches the site's dark surface. */
    private static final Color BACKGROUND = new Color(0x1d, 0x22, 0x2b);

    private ImageResizer() {
    }

    public static byte[] toJpeg(InputStream in, int maxWidth, int maxHeight) throws IOException {
        BufferedImage src = ImageIO.read(in);
        if (src == null) {
            throw new IOException("Not a supported image");
        }
        // Flatten onto an opaque background first, so transparent areas don't turn black.
        BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(src, 0, 0, null);
        g.dispose();

        double scale = Math.min(1.0, Math.min((double) maxWidth / rgb.getWidth(), (double) maxHeight / rgb.getHeight()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(rgb)
                .scale(scale)
                .outputFormat("jpg")
                .outputQuality(0.93)
                .toOutputStream(out);
        return out.toByteArray();
    }
}
