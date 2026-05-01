package net.zicai.test;

import lombok.extern.slf4j.Slf4j;
import net.zicai.service.OssService;
import net.zicai.config.OssClientManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 短信发送测试类
 * 使用 common 模块封装的 SmsClient 发送短信
 *
 * @author 王镝
 * @date 2026-04-15
 */
@Slf4j
@SpringBootTest
public class OssManagerTest {

    @Autowired
    private  OssClientManager ossClientManager;

    @Autowired
    private OssService ossService;


    @Test
    public void testDeleteFile(){

        //获取文件Url
        String fileUrl = "https://zccloud-interview-pub.oss-cn-beijing.aliyuncs.com/%E7%AE%80%E5%8E%861.pdf";

        //判断bucket
        OssClientManager.BucketClient bucket = ossClientManager.resolveByUrl(fileUrl);
        //获取fileKey
        String fileKey = ossService.extractFileKey(fileUrl);
        //删除
        bucket.getClient().deleteObject(bucket.getBucketName(), fileKey);
        log.info("删除文成功 fileUrl：{}",fileUrl);
    }

    @Test
    public void testGenerateUrl(){

        String fileUrl = "https://zccloud-interview-pri.oss-cn-beijing.aliyuncs.com/.pdf";

        OssClientManager.BucketClient bucketClient = ossClientManager.resolveByUrl(fileUrl);

        if(bucketClient.isPublic()){
            log.info(fileUrl);
        }else{
            log.info(ossService.generatePreviewUrl(fileUrl));
        }

    }

}
