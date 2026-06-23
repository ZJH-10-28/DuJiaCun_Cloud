package dujiacun.common.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BaseModel implements Serializable {
    /**
     *用于校验序列化与反序列化时类结构的一致性。避免兼容性问题
     */
    private static final long serialVersionUID = 1L;

    /**
     * 创建时间
     */
    protected Date createTime;

    /**
     * 修改时间
     */
    protected Date updateTime;

    /**
     * 创建人
     */
    protected String createUser;

    /**
     * 修改人
     */
    protected String updateUser;

    /**
     * 删除标志
     */
    protected int delFlag;
}
