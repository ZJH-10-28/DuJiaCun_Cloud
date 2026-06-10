package dujiacun.skuservice.mapper;

import dujiacun.skuservice.entity.SkuEntity;
import dujiacun.skuservice.entity.SkuParamBo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SkuMapper {

    List<SkuEntity> getSkuInfo(@Param("skuParamBo") SkuParamBo skuParamBo);

    SkuEntity getBySkuId(@Param("skuId") Long skuId);

    int insertSkuInfo(@Param("skuParamBo") SkuParamBo skuParamBo);

    int updateSkuInfo(@Param("skuParamBo") SkuParamBo skuParamBo);

    int saleSkuInfo(
            @Param("skuId") Long skuId,
            @Param("saleCount") Integer saleCount
    );

    int deleteSkuInfo(@Param("skuId") Long skuId);
}
