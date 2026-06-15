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

    List<SkuEntity> getSkuInfoById(@Param("skuIds") List<Long> skuIds);

    List<SkuEntity> getSkuStocksByIds(@Param("skuIds") List<Long> skuIds);

    int insertSkuInfo(@Param("skuParamBo") SkuParamBo skuParamBo, @Param("deleteStatus") Integer deleteStatus);

    int saveSkuDetail(
            @Param("orderId") Long orderId,
            @Param("skuStockList") List<SkuStock> skuStockList,
            @Param("orderStatus") Integer orderStatus
    );

    int updateSkuInfo(@Param("skuParamBo") SkuParamBo skuParamBo);

    int saleSkuInfo(@Param("orderId") Long orderId, @Param("saleStatus") Integer saleStatus);

    int deleteSkuInfo(@Param("skuId") List<Long> skuIds, @Param("deleteStatus") Integer deleteStatus);
}
