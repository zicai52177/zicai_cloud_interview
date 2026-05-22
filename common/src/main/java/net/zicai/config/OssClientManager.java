package net.zicai.config;


import com.aliyun.oss.OSS;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * @author 王镝
 * @date 20260423
 **/

@Getter
@AllArgsConstructor
public class OssClientManager{

    //私有
    private final BucketClient secureBucket;
    //公共
    private final BucketClient publicBucket;

    /**
     * 智能路由方法
     */
    public BucketClient resolveByUrl(String fileUrl){

        //参数校验
        if(StringUtils.isBlank(fileUrl)){
            return null;
        }

        //判断
        if(secureBucket.matchesDomain(fileUrl)){
            return secureBucket;
        }
        if(publicBucket.matchesDomain(fileUrl)){
            return publicBucket;
        }

        return null;
    }

    @Getter
    public static final class BucketClient{
        /**
         * OSS客户端
         */
        private final OSS client;
        /**
         * 存储桶名称
         */
        private final String bucketName;
        /**
         * 域名
         */
        private final String domain;
        /**
         * 判断是否是公共
         */
        private final boolean isPublic;



        public BucketClient(OSS client, String bucketName, String domain, boolean isPublic){
             this.client = client;
             this.bucketName = bucketName;
             this.domain = domain;
             this.isPublic = isPublic;
        }

        public boolean hasBucket(){
            return bucketName != null && !"".equals(bucketName);
        }

        public boolean hasClient(){
            return client != null;
        }

        public boolean hasDomain(){
            return domain != null && !"".equals(domain);
        }

        /**
         * 判断域名是否带斜杠,返回带斜杠的域名
         */

        public String domainwithSlash(){
            if(hasDomain()){
                return domain.endsWith("/") ? domain : domain + "/";
            }
            return "";
        }

        /**
         * 构建文件的fileKey
         */
        public String buildFileKey(String fileKey){
            return domainwithSlash() + fileKey;
        }

        /**
         * 判断Url是否以指定域名开头
         */

        public boolean matchesDomain(String url){
            return hasDomain() && url.startsWith(domainwithSlash());
        }

        /**
         * 统一域名格式,避免`https://public.xdclass.net/`这种带斜杠的情况
         * @param domain
         * @return
         */
        private static String normalizeDomain(String domain) {
            if (domain == null) {
                return "";
            }
            String value = domain.trim();
            // 移除末尾的所有斜杠
            while (value.endsWith("/")) {
                value = value.substring(0, value.length() - 1);
            }
            return value;
        }
    }


}
