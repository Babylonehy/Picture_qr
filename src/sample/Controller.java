package sample;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
import java.net.URL;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

import static sample.KvPair.ossClient;


public class Controller {

    private Window stage;
    @FXML private Label bottom_text;
    @FXML private ImageView imageView_pic;
    @FXML private ImageView imageView_QR;


    //Initial Config
    String endpoint = "";
    // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
    String accessKeyId = "";
    String accessKeySecret = "";
    String bucketName = "";

    final String CONFIG_PATH="src/sample/Config.json";
    final String QR_PATH="qrcode.png";
    private KvPair CONFIG_OBJ=null;
    boolean CONFIG_Set=false;
    String QRapi="";
    String url="";
    /**
     * listciew数据
     */
    private ObservableList<String> dataList = FXCollections.observableArrayList();
    private ObservableMap<String,String> dataMap = FXCollections.observableHashMap();
    @FXML private ListView listView;

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
                new FileChooser.ExtensionFilter("All Images", "*.png","*.jpg","*.gif","*.bmp"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp")

        );
        File file=fileChooser.showOpenDialog(stage);

        float fileSize=getFileSize(file);
        if (fileSize!=-1){
            bottom_text.setText("PicName: "+file.getName()+"\nSize:  "+fileSize+"kb");
            String path=file.getPath();
            path="file:/"+path.replaceAll("\\\\","/");
            System.out.printf(path);
            imageView_pic.setImage(new Image(path));
            return true;
        }
        else {
            bottom_text.setText("File not existed.");
            return false;
        }


    }

    /**
     *
     * @param file
     * @return
     */
    private boolean Uploadfile(File file,KvPair ossPair){

        return false;
    }

    /**
     *
     * @return
     */
    @FXML
    private KvPair Readconfig() throws IOException {
        // 读取原始json文件
        String rtn="";
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
            System.out.println(dataJson.toString());
            QRapi=dataJson.getString("QRapi");
            endpoint = dataJson.getJSONObject("oss").getString("endPoint");
            accessKeyId= dataJson.getJSONObject("oss").getString("accessKeyId");
            accessKeySecret= dataJson.getJSONObject("oss").getString("accessKeySecret");
            bucketName= dataJson.getJSONObject("oss").getString("bucketName");



            CONFIG_Set=true;
            renewBottom("Config Successful."+"  "+url);
            System.out.println(url);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 创建OSSClient实例。
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        KvPair ossPair=new KvPair(bucketName,ossClient);
        CONFIG_OBJ=ossPair;

        return ossPair;

    }

    @FXML
    private  void  Listfile(){
        if (CONFIG_Set){
        // ossClient.listObjects返回ObjectListing实例，包含此次listObject请求的返回结果。
        ObjectListing objectListing = ossClient.listObjects(CONFIG_OBJ.bucketName);
        // objectListing.getObjectSummaries获取所有文件的描述信息。
        for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            System.out.println(" - " + objectSummary.getKey() + "  " +
                    "(size = " + objectSummary.getSize() + ")");
            dataList.add(objectSummary.getKey());
            dataMap.put(objectSummary.getKey(), String.valueOf(objectSummary.getSize()));
        }

        // 关闭OSSClient。
        KvPair.shutdown();
//        System.out.println(dataList);
        listView.setItems(dataList);

        }
        else {
            renewBottom("Config failed.");
        }
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

    private void renewBottom(String txt){
        //TODO Updae this to log
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
        String org =bottom_text.getText();
        bottom_text.setText(ft.format(date)+"  "+txt+"\n"+org);
    }
    /**
     *
     * @return
     */
    @FXML
    private boolean GenerateQR() throws IOException {

       if (CONFIG_Set){

           String objName=GetItem(listView);
            if (!objName.equals("")&&!objName.equals("null")){
                // 设置图片处理样式。
                String style = "image/resize,m_fixed,w_100,h_100/rotate,90";
                // 指定过期时间为10分钟。
                Date expiration = new Date(new Date().getTime() + 1000 * 60 * 10 );
                GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucketName, objName, HttpMethod.GET);
                req.setExpiration(expiration);
                req.setProcess(style);
                URL signedUrl = CONFIG_OBJ.ossClient.generatePresignedUrl(req);
                System.out.println(signedUrl);
                // 关闭OSSClient。
                CONFIG_OBJ.ossClient.shutdown();
                new QRgenerate().genQrcode(String.valueOf(signedUrl));
                Image img=new Image(QR_PATH);
                imageView_QR.setImage(img);
            }
           return true;
       }
       else {
           renewBottom("Wrong config.");
           return false;
       }



    }
    private String GetItem(ListView listView){
        String rtn="";
        if (listView.getSelectionModel().selectedItemProperty()!=null){
            rtn= String.valueOf(listView.getSelectionModel().selectedItemProperty().getValue());
        }
        System.out.println(rtn);
        return rtn;
    }
}
