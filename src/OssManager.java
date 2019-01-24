import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.PutObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.Date;

public class OssManager {

    private OSSClient ossClient;
    private boolean DisConnected = true;

    //Initial Config
    private String bucketName = "";
    private String endpoint = "";
    private String accessKeyId = "";
    private String accessKeySecret = "";
    private String style="";


    /**
     * @param bucketName
     * @param endpoint
     * @param accessKeyId
     * @param accessKeySecret
     */
    OssManager(String bucketName, String endpoint, String accessKeyId, String accessKeySecret) {
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.bucketName = bucketName;
        this.ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        this.DisConnected = false;

    }

    /**
     * @param bucketName
     * @param ossClient
     */
    OssManager(String bucketName, OSSClient ossClient) {
        this.endpoint = String.valueOf(ossClient.getEndpoint());
        this.bucketName = bucketName;
        this.ossClient = ossClient;
        this.DisConnected = false;
    }

    /**
     * @param confing_path
     */
    OssManager(String confing_path) {
        try {
            ReadConfig(confing_path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        this.DisConnected = false;
    }

    /**
     *
     */
    void shutdown() {
        if (!DisConnected) {
            this.DisConnected = true;
            ossClient.shutdown();
        }
    }

    /**
     * @return bucketName
     */
    String getBucketName() {
        return bucketName;
    }

    String getEndpoint() {
        return endpoint;
    }

    OSSClient getOssClient() {
        return ossClient;
    }

    boolean isDisConnected() {
        return !DisConnected;
    }

    boolean Delete(String filename) {
        if (isDisConnected()) {
            ossClient.deleteObject(bucketName, filename);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param path
     * @return
     * @throws IOException
     */
    boolean ReadConfig(String path) throws IOException {

        if (DisConnected) {
            // 读取原始json文件
            String rtn = "";
            BufferedReader config = new BufferedReader(new FileReader(path));
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
                endpoint = dataJson.getJSONObject("oss").getString("endPoint");
                accessKeyId = dataJson.getJSONObject("oss").getString("accessKeyId");
                accessKeySecret = dataJson.getJSONObject("oss").getString("accessKeySecret");
                bucketName = dataJson.getJSONObject("oss").getString("bucketName");
                style=dataJson.getJSONObject("oss").getString("style");

                return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        return false;

    }

    /**
     * @param objName
     * @param style
     * @param expiration_min
     * @return
     */
    URL UrlRequest(String objName,String style,int expiration_min){
        if (isDisConnected()){
            Date expiration = new Date(new Date().getTime() + 1000 * 60 * expiration_min);
            GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucketName, objName, HttpMethod.GET);
            req.setExpiration(expiration);
            req.setProcess(style);
            URL signedUrl = ossClient.generatePresignedUrl(req);
            return signedUrl;
        }

        return null;
    }

    /**
     * @param objName
     * @param expiration_min
     * @return
     */
    URL UrlRequest(String objName, int expiration_min){
        return UrlRequest(objName,style,expiration_min);
    }

    /**
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    boolean Uploadfile(File file) throws FileNotFoundException {
        if (file != null && isDisConnected()) {
            InputStream inputStream = new FileInputStream(file);
            //ossPair.getOssClient().putObject(ossPair.getBucketName(), file.getName(), inputStream);
            ossClient.putObject(new PutObjectRequest(bucketName, file.getName(), inputStream).
                    <PutObjectRequest>withProgressListener(new PutObjectProgressListener()));
            return true;
        } else {
            return false;
        }


    }
}
