package net.zicai.util;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Spring Bean 工具类，提供对象属性拷贝功能
 */
public class SpringBeanUtil {

    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象属性拷贝失败", e);
        }
    }

    public static <T> List<T> copyProperties(Collection<?> sourceList, Class<T> targetClass) {
        List<T> result = new ArrayList<>();
        if (sourceList == null || sourceList.isEmpty()) {
            return result;
        }
        for (Object source : sourceList) {
            result.add(copyProperties(source, targetClass));
        }
        return result;
    }
}
