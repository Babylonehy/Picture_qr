import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.PutObjectRequest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Controller {

    private Window stage;
    @FXML
    private Label bottom_text;
    @FXML
    private ImageView imageView_pic;
    @FXML
    private ImageView imageView_QR;
    @FXML
    static ProgressBar progressBar;


    //Initial Config
    private String endpoint = "";
    // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
    private String accessKeyId = "";
    private String accessKeySecret = "";
    private String bucketName = "";

    private final String CONFIG_PATH = "src/Config.json";
    private OssManager OSS_OBJ = null;
    private boolean CONFIG_State = false;
    private String url = "";

    // Pic related
    final String style = "image/resize,m_fixed,w_1080,h_1920";

    /**
     * listciew数据
     */
    private ObservableList<String> dataList = FXCollections.observableArrayList();
    private ObservableMap<String, String> dataMap = FXCollections.observableHashMap();
    @FXML
    private ListView listView;

    /**
     * @return
     */
    @FXML
    private boolean Loadfile() throws IOException {

        if (OSS_OBJ==null||!OSS_OBJ.isDisConnected()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("View Pictures");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))
            );
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Images", "*.png", "*.jpg", "*.gif", "*.bmp"),
                    new FileChooser.ExtensionFilter("PNG", "*.png"),
                    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                    new FileChooser.ExtensionFilter("GIF", "*.gif"),
                    new FileChooser.ExtensionFilter("BMP", "*.bmp")

            );
            File file = fileChooser.showOpenDialog(stage);

            float fileSize = getFileSize(file);
            if (fileSize != -1) {
                bottom_text.setText("PicName: " + file.getName() + "\nSize:  " + fileSize + "kb");
                String path = file.getPath();
                path = "file:/" + path.replaceAll("\\\\", "/");
                System.out.printf(path);

                imageView_pic.setImage(new Image(path));
                if (Uploadfile(file, OSS_OBJ)) {
                    return true;
                }
                return false;
            } else {
                bottom_text.setText("File not existed.");
                return false;
            }

        } else {
            bottom_text.setText("No Connection to OSS.");
            return false;
        }

    }

    /**
     * @param file
     * @return
     */
    private boolean Uploadfile(File file, OssManager ossPair) throws FileNotFoundException {
        if (file != null && ossPair != null) {
            InputStream inputStream = new FileInputStream(file);
            //ossPair.getOssClient().putObject(ossPair.getBucketName(), file.getName(), inputStream);
            ossPair.getOssClient().putObject(new PutObjectRequest(ossPair.getBucketName(), file.getName(), inputStream).
                    <PutObjectRequest>withProgressListener(new PutObjectProgressListener()));
            renewBottom("Succeed to upload!");
            return true;
        } else {
            renewBottom("file or ossClientObj is null!");
            return false;
        }


    }

    @FXML
    private void Deletefile() {
        String filename = GetItem(listView);
        if (OSS_OBJ.Deletefile(filename)) {
            Listfile();
            renewBottom("Delete " + filename + " Successful");
        } else {
            renewBottom("Failed Delete" + filename);
        }
    }

    @FXML
    private void CloseConnect() {
        if (OSS_OBJ!=null&&!OSS_OBJ.isDisConnected()) {
            CONFIG_State=false;
            OSS_OBJ.shutdown();
            OSS_OBJ=null;
            renewBottom("Close Connection Successfully.");
        } else {
            renewBottom("Failed. No Connection.");
        }
    }

    /**
     * @return
     */
    @FXML
    private void ConnectToOss() throws IOException {

        if (!CONFIG_State && OSS_OBJ == null) {

            // 读取原始json文件
            String rtn = "";
            BufferedReader config = new BufferedReader(new FileReader(CONFIG_PATH));
            try {
                String temp = null;
                while ((temp = config.readLine()) != null) {
                    rtn += "" + temp;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            try {
                JSONObject dataJson = new JSONObject(rtn);
                System.out.println(dataJson.toString());
                endpoint = dataJson.getJSONObject("oss").getString("endPoint");
                accessKeyId = dataJson.getJSONObject("oss").getString("accessKeyId");
                accessKeySecret = dataJson.getJSONObject("oss").getString("accessKeySecret");
                bucketName = dataJson.getJSONObject("oss").getString("bucketName");
                // 创建OSSClient实例。
                OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
                OssManager ossPair = new OssManager(bucketName, ossClient);
                OSS_OBJ = ossPair;
                CONFIG_State = true;
                renewBottom("Connect Successful." + "  " + url);
                System.out.println(url);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        } else {
            renewBottom("Already exist Connection.");
        }
    }

    @FXML
    private void Listfile() {

        dataList.clear();
        dataMap.clear();
        if (CONFIG_State) {
            // ossClient.listObjects返回ObjectListing实例，包含此次listObject请求的返回结果。
            ObjectListing objectListing = OSS_OBJ.getOssClient().listObjects(OSS_OBJ.getBucketName());
            // objectListing.getObjectSummaries获取所有文件的描述信息。
            for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                System.out.println(" - " + objectSummary.getKey() + "  " +
                        "(size = " + objectSummary.getSize() + ")");
                dataList.add(objectSummary.getKey());
                dataMap.put(objectSummary.getKey(), String.valueOf(objectSummary.getSize()));
            }

//        System.out.println(dataList);
            listView.setItems(dataList);
            listView.refresh();
        } else {
            renewBottom("Config failed.(In root, NO config.json or Wrong config.json)");
        }
    }

    /**
     * @param file
     * @return
     */
    private float getFileSize(File file) {
        if (file.exists() || file.isFile()) {
            return (float) (file.length() * 0.001);
        } else {
            return -1;
        }
    }

    /**
     * @param txt
     */
    private void renewBottom(String txt) {
        //TODO Update this to log
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String old = bottom_text.getText();
        bottom_text.setText(ft.format(date) + "  " + txt + "\n" + old);

    }

    /**
     * @return
     */
    @FXML
    private boolean GenerateQR() throws IOException {

        if (CONFIG_State) {

            String objName = GetItem(listView);
            if (!objName.equals("") && !objName.equals("null")) {
                // 设置图片处理样式。

                // 指定过期时间为10分钟。
                Date expiration = new Date(new Date().getTime() + 1000 * 60 * 10);
                GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucketName, objName, HttpMethod.GET);
                req.setExpiration(expiration);
                req.setProcess(style);
                URL signedUrl = OSS_OBJ.ossClient.generatePresignedUrl(req);
                System.out.println(signedUrl);
                // 关闭OSSClient。
                OSS_OBJ.shutdown();
                String name = "QR" + (new Date().getTime()) + ".png";
                QRgenerate qr = new QRgenerate(String.valueOf(signedUrl), name);
                File pic = new File(qr.getPath());
                InputStream is = new FileInputStream(pic);
                Image img = new Image(is);
                imageView_QR.setImage(img);
            }
            return true;
        } else {
            renewBottom("Wrong config.");
            return false;
        }


    }

    /**
     * @param listView
     * @return
     */
    private String GetItem(ListView listView) {
        String rtn = "";
        if (listView.getSelectionModel().selectedItemProperty() != null) {
            rtn = String.valueOf(listView.getSelectionModel().selectedItemProperty().getValue());
        }
        System.out.println(rtn);
        return rtn;
    }
}
