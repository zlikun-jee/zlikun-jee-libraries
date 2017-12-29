package com.zlikun.learning;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成二维码
 * @author zlikun <zlikun-dev@hotmail.com>
 * @date 2017-12-29 10:19
 */
@Slf4j
public class QrcodeTest {

    @Test
    public void test() {

        generate(ErrorCorrectionLevel.L);
        generate(ErrorCorrectionLevel.M);
        generate(ErrorCorrectionLevel.Q);
        generate(ErrorCorrectionLevel.H);
    }

    /**
     * 测试生成二维码，测试其容错率
     * @param level
     */
    private void generate(ErrorCorrectionLevel level) {
        final int width = 300;
        final int height = 300;
        final String format = "png";

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 设置容错率，默认L，取值范围：L(7%)、M(15%)、Q(25%)、H(%30)
        hints.put(EncodeHintType.ERROR_CORRECTION, level);
        // 设置白色边框值(margin)，设置为0时，没有边框
        hints.put(EncodeHintType.MARGIN, 2);

        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Path path = Paths.get("target\\qrcode-" + level + "." + format);
            // 生成一张黑色背景图
            ImageIO.write(image, format, path.toFile());
            // 生成二维码图案
            BitMatrix bitMatrix = new QRCodeWriter()
                    .encode("http://www.zhihuishu.com",
                            BarcodeFormat.QR_CODE,
                            width,
                            height,
                            hints);
            MatrixToImageWriter.writeToPath(bitMatrix, format, path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("二维码生成完成 ...");
    }

}
