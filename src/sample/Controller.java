package sample;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class Controller {

    private Window stage;
    @FXML private Label bottom_text;
    @FXML private ImageView imageView_pic;
    @FXML private ImageView imageView_QR;

    //Config
    final String CONFIG_PATH="src/sample/Config.json";
    private KvPair CONFIG_OBJ=null;
    /**
     *
     * @return
     */
    @FXML
    private boolean Loadfile() throws IOException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("View Pictures");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                //new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp")

        );
        File file=fileChooser.showOpenDialog(stage);
        //System.out.println(file);

        float fileSize=getFileSize(file);
        if (fileSize!=-1){
            bottom_text.setText("PicName: "+file.getName()+"\nSize:  "+fileSize+"kb");

            String path=file.getPath();
            path="file:/"+path.replaceAll("\\\\","/");
            System.out.printf(path);
            imageView_pic.setImage(new Image(path));

        }
        else {
            bottom_text.setText("File not existed.");
        }

        return false;
    }

    /**
     *
     * @param file
     * @return
     */
    private boolean Uploadfile(File file,KvPair ossPair){

        // ossClient.listObjects返回ObjectListing实例，包含此次listObject请求的返回结果。
        ObjectListing objectListing = ossPair.ossClient.listObjects(ossPair.bucketName);
        // objectListing.getObjectSummaries获取所有文件的描述信息。
        for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            System.out.println(" - " + objectSummary.getKey() + "  " +
                    "(size = " + objectSummary.getSize() + ")");
        }

        // 关闭OSSClient。
        KvPair.shutdown();

        return false;
    }

    /**
     *
     * @return
     */
    private KvPair Readconfig() throws IOException {

        // 读取原始json文件
        String rtn=null;
        BufferedReader config = new BufferedReader(new FileReader(CONFIG_PATH));
        try {
            String temp=null;
             while ((temp = config.readLine()) != null) {
              rtn+=""+temp;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //
        try {

            JSONObject dataJson = new JSONObject(rtn);// 创建一个包含原始json串的json对象


            System.out.println(dataJson.get("QRapi").toString());
        } catch (JSONException e) {

            e.printStackTrace();
        }
       // System.out.printf(rtn);
            // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = "http://oss-cn-beijing.aliyuncs.com";
        // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
        String accessKeyId = "<yourAccessKeyId>";
        String accessKeySecret = "<yourAccessKeySecret>";
        String bucketName = "<yourBucketName>";

        // 创建OSSClient实例。
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        KvPair ossPair=new KvPair(bucketName,ossClient);
        CONFIG_OBJ=ossPair;

        return ossPair;

    }

    @FXML
    private  void  Listfile(){

    }

    /**
     *
     * @param file
     * @return
     */
    private  float getFileSize(File file){
        if (file.exists() || file.isFile()){
            return (float) (file.length()*0.001);
        }
        else {
            return -1;
        }
    }

    /**
     *
     * @return
     */
    @FXML
    private boolean GenerateQR() throws IOException {

        Readconfig();
        Image img=new Image("https://lai.yuweining.cn");
        imageView_QR.setImage(img);

        return false;
    }

}
