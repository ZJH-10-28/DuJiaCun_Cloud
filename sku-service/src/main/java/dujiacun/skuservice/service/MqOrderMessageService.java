package dujiacun.skuservice.service;

import dujiacun.common.CommonResult;
import dujiacun.common.error.ErrorCode;
import dujiacun.common.exception.BusinessException;
import dujiacun.skuservice.entity.MqConsumeLog;
import dujiacun.skuservice.mapper.MqConsumeLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dujiacun.common.constant.RabbitMQConstant.MQ_BIZ_TYPE_ORDER_STOCK_DEDUCT;
import static dujiacun.common.constant.RabbitMQConstant.MQ_CONSUME_STATUS_SUCCESS;

@Service
public class MqOrderMessageService {

    @Autowired
    private MqConsumeLogMapper mqConsumeLogMapper;

    @Autowired
    private ISkuService skuService;

    @Transactional(rollbackFor = Exception.class)
    public boolean consumeOrderStockDeduct(String messageId, Long orderId) {
        MqConsumeLog consumeLog = new MqConsumeLog();
        consumeLog.setMessageId(messageId);
        consumeLog.setBizType(MQ_BIZ_TYPE_ORDER_STOCK_DEDUCT);
        consumeLog.setBizId(orderId.toString());
        consumeLog.setConsumeStatus(MQ_CONSUME_STATUS_SUCCESS);

        int inserted = mqConsumeLogMapper.insertIgnore(consumeLog);
        if (inserted == 0) {
            return false;
        }

        CommonResult<String> result = skuService.saleSkuInfo(orderId);
        if (!result.getCode().equals(ErrorCode.SUCCESS.getCode())) {
            throw new BusinessException("库存扣减失败");
        }
        return true;
    }
}
