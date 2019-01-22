package sample;

import com.aliyun.oss.OSSClient;

public class KvPair {
    String bucketName;
    static OSSClient ossClient;

    KvPair(String bucketName, OSSClient ossClient){
        this.bucketName=bucketName;
        this.ossClient=ossClient;
    }

    static void shutdown(){

        ossClient.shutdown();
    }
}
