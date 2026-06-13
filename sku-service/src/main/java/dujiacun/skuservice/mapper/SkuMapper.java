package dujiacun.skuservice.mapper;

import dujiacun.skuservice.entity.SkuEntity;
import dujiacun.skuservice.entity.SkuParamBo;
import dujiacun.skuservice.entity.SkuStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SkuMapper {

    List<SkuEntity> getSkuInfo(@Param("skuParamBo") SkuParamBo skuParamBo);

    SkuEntity getSkuInfoById(@Param("skuId") Long skuId);

    Integer getSkuStockCountById(@Param("skuId") Long skuId);

    int insertSkuInfo(@Param("skuParamBo") SkuParamBo skuParamBo);

    int saveSkuDetail(
            @Param("orderId") Long orderId,
            @Param("skuStockList") List<SkuStock> skuStockList
    );

    int updateSkuInfo(@Param("skuParamBo") SkuParamBo skuParamBo);

    int saleSkuInfo(
            @Param("skuId") Long skuId,
            @Param("saleCount") Integer saleCount
    );

    int deleteSkuInfo(@Param("skuId") Long skuId);
}
