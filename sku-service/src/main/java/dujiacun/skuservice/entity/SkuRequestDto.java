package dujiacun.skuservice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SkuRequestDto {
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 商品名称
     */
    private String skuName;
    /**
     * 商品介绍
     */
    private String skuDescription;
    /**
     * 分类id
     */
    private Integer categoryId;
    /**
     * 价格
     */
    private Double skuPrice;
    /**
     * 销量
     */
    private Integer saleCount;
    /**
     * 库存
     */
    private Integer skuStockCount;
    /**
     * 商品状态[0 - 下架，1 - 上架]
     */
    private Integer skuStatus;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 创建人
     */
    private Long createUser;
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
    /**
     * 修改人
     */
    private Long updateUser;
    /**
     * 逻辑删除
     */
    private Integer isDeleted;

}
