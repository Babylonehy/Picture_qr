package sample;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QRgenerate {
      public void genQrcode(String message) {
        //输出目标文件
        File file = new File("src/qrcode.png");
        if (!file.exists()) {
            try {
                file.mkdirs();
                file.createNewFile();
            } catch (IOException e) {}
        }
        //设置参数，输出文件
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.QR_CODE,
                    300, 300, hints);// 生成矩阵

           MatrixToImageWriter.writeToPath(bitMatrix, "png", file.toPath());// 输出图像
        } catch (Exception e) {}
    }


}
