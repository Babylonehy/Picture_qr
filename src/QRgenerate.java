import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class QRgenerate {
    private  String message;
    private  String name;
    private  String path;
    private  String QR_PATH="src/res/QR_";
    private Date time =new Date();
    private SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");

    /**
     *
     * @param message
     * @param name
     */


    QRgenerate(String message, String name, String QR_PATH) {
          this.message=message;
          this.name=name;
          this.QR_PATH=QR_PATH;

        //输出目标文件
        File file = new File(QR_PATH+ft.format(time)+"/"+name);
        this.path=file.getPath();
        generateQR(file);
    }

    QRgenerate(String message, String name){
        this.message=message;
        this.name=name;

        //输出目标文件
        File file = new File(QR_PATH+ft.format(time)+"/"+name);
        this.path=file.getPath();
        generateQR(file);

    }

    public String getPath() {
        return path;
    }

    boolean generateQR(File file){

        if (!file.exists()) {
            try {
                file.mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //设置参数，输出文件
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.QR_CODE,
                    300, 300, hints);// 生成矩阵

            MatrixToImageWriter.writeToPath(bitMatrix, "png", file.toPath());// 输出图像
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
