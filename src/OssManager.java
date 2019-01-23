import com.aliyun.oss.OSSClient;

public class OssManager {
    String bucketName;
    static OSSClient ossClient;
    private boolean DisConnected = false;

    OssManager(String bucketName, OSSClient ossClient) {
        this.bucketName = bucketName;
        this.ossClient = ossClient;

    }

    void shutdown() {
        this.DisConnected = true;
        ossClient.shutdown();
    }

    public String getBucketName() {
        return bucketName;
    }

    public static OSSClient getOssClient() {
        return ossClient;
    }

    public boolean isDisConnected() {
        return DisConnected;
    }

    public boolean Deletefile(String filename) {
        if (!isDisConnected()) {
            ossClient.deleteObject(bucketName, filename);
            return true;
        } else {
            return false;
        }
    }
}
