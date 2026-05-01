package net.zicai.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.zicai.service.FileService;
import net.zicai.service.OssService;
import net.zicai.config.OssClientManager;
import net.zicai.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author 王镝
 * @date @date 20260425 10:22
 **/

@Service
@Slf4j
public class FileServiceImpl implements FileService {


    private static final Long MAX_FILE_SIZE = 10 * 1024 * 1024L;

    private static final String DEFAULT_PUBLIC_FILE_FOLDER = "public/file";
    private static final String DEFAULT_SECURE_FILE_FOLDER = "secure/file";

    @Autowired
    private OssClientManager ossClientManager;

    @Autowired
    private OssService ossService;

    @Override
    public JsonData uploadPublicFile(MultipartFile file, String folder) {
        String ResultUrl = uploadFileToPublicBucket(file, folder == null ? DEFAULT_PUBLIC_FILE_FOLDER : folder);
        return JsonData.buildSuccess(Map.of("Url",ResultUrl));
    }

    @Override
    public JsonData uploadPublicFile(MultipartFile file) {
        String ResultUrl = uploadFileToPublicBucket(file,DEFAULT_PUBLIC_FILE_FOLDER);
        return JsonData.buildSuccess(Map.of("Url",ResultUrl));
    }

    @Override
    public JsonData uploadSecureFile(MultipartFile file, String folder) {
        String ResultUrl = uploadFileToSecureBucket(file, folder == null ? DEFAULT_SECURE_FILE_FOLDER : folder );
        return JsonData.buildSuccess(Map.of("Url",ResultUrl));
    }

    @Override
    public JsonData uploadSecureFile(MultipartFile file) {
        String ResultUrl = uploadFileToSecureBucket(file, DEFAULT_SECURE_FILE_FOLDER);
        return JsonData.buildSuccess(Map.of("Url",ResultUrl));
    }
    @Override
    public boolean deleteOldAvatar(String fileUrl) {
        return ossService.deleteFile(fileUrl);
    }
    private String uploadFileToPublicBucket(MultipartFile file,String folder) {
        if(validate(file)){
            String fileUrl = ossService.uploadPublicFile(file,folder);
            log.info("上传成功，可访问的Url为：{}",fileUrl);
            return fileUrl;
        }
        return null;
    }
    private String uploadFileToSecureBucket(MultipartFile file,String folder) {
        if(validate(file)){
            String fileUrl = ossService.uploadSecureFile(file,folder);
            log.info("上传成功，可访问的Url为：{}",fileUrl);
            return fileUrl;
        }
            return  null;
    }



    private boolean validate(MultipartFile file) {
        if(file == null || file.isEmpty()){
            throw new RuntimeException("文件不存在");
        }
        if(file.getSize() > MAX_FILE_SIZE){
            throw new RuntimeException("文件过大，请重新上传");
        }
        String fileName = file.getOriginalFilename();
        if(fileName == null){
            throw new RuntimeException("不支持此类文件");
        }
        return true;


    }

}
