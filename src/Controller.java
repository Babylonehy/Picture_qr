import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
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
    private ProgressBar progressBar;
    @FXML
    private Label bottom_textR;

    private final String CONFIG_PATH = "src/Config.json";
    private OssManager OSS_OBJ = null;
    private boolean CONFIG_State = false;

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
    private void ConnectToOss() throws IOException {
        if (!CONFIG_State && OSS_OBJ == null) {
            OssManager ossPair = new OssManager(CONFIG_PATH);
            OSS_OBJ = ossPair;
            CONFIG_State = true;
            Listfile();
            bottom_textR.setText("Endpoint: " + OSS_OBJ.getEndpoint() + "\nBucketName: " + OSS_OBJ.getBucketName());
            renewBottom("Connect Successful.");
        } else {
            renewBottom("Already exist Connection.");
        }

    }


    @FXML
    private void CloseConnect() {
        if (OSS_OBJ != null && OSS_OBJ.isDisConnected()) {
            CONFIG_State = false;
            OSS_OBJ.shutdown();
            OSS_OBJ = null;
            bottom_textR.setText("");
            renewBottom("Close Connection Successfully.");
        } else {
            renewBottom("Failed. No Connection.");
        }
    }



    /**
     * @return
     */
    @FXML
    private boolean Loadfile() throws IOException {

        if (OSS_OBJ == null || OSS_OBJ.isDisConnected()) {
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
                renewBottom("PicName: " + file.getName() + "    Size: " + fileSize + "kb");
                String path = file.getPath();
                path = "file:/" + path.replaceAll("\\\\", "/");
                System.out.printf(path + "\n");

                imageView_pic.setImage(new Image(path));
                if (Uploadfile(file)) {
                    return true;
                }
                return false;
            } else {
                renewBottom("File not existed.");
                return false;
            }

        } else {
            renewBottom("No Connection to OSS.");
            return false;
        }

    }

    /**
     * @param file
     * @return
     */
    private boolean Uploadfile(File file) throws FileNotFoundException {
        if (file != null && OSS_OBJ != null) {
            if (OSS_OBJ.Uploadfile(file)) {
                renewBottom("Succeed to upload!");
                Listfile();
                return true;
            }

        } else {
            renewBottom("File or ossClientObj is null!");
        }
        return false;

    }

    /**
     *
     */
    @FXML
    private void Deletefile() {
        String filename = GetItem(listView);
        if (OSS_OBJ.Delete(filename)) {
            Listfile();
            renewBottom("Delete [" + filename + "] Successful");
        } else {
            renewBottom("Failed Delete" + filename);
        }
    }

    /**
     *
     */
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
     * @return
     */
    @FXML
    private boolean GenerateQR() throws IOException {

        if (CONFIG_State) {
            String objName = GetItem(listView);
            if (!objName.equals("") && !objName.equals("null")) {
                URL signedUrl = OSS_OBJ.UrlRequest(objName, 10);
                System.out.println(signedUrl);
                String name = "QR" + (new Date().getTime()) + ".png";
                QRgenerate qr = new QRgenerate(String.valueOf(signedUrl), name);
                File pic = new File(qr.getPath());
                InputStream is = new FileInputStream(pic);
                Image img = new Image(is);
                imageView_QR.setImage(img);
                renewBottom("GenerateQR [" + name + "] Successful.");
            }

            return true;
        } else {
            renewBottom("GenerateQR Failed.");
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

}
