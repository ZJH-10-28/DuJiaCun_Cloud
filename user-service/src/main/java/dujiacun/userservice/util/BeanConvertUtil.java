package dujiacun.userservice.util;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;

public class BeanConvertUtil {

    /**
     * DTO转BO（通用方法）
     * @param source 源对象（DTO）
     * @param targetClass 目标类（BO的Class）
     * @return 映射后的BO对象
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        if (ObjectUtils.isEmpty(source)) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            // 核心方法：拷贝同名字段，忽略null值（可选）
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("convert Failed", e);
        }
    }
}
