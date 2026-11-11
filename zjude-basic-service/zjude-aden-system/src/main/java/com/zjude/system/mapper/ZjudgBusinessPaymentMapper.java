package com.zjude.system.mapper;

import org.apache.ibatis.annotations.Param;
import com.zjude.system.domain.ZjudgBusinessPayment;

public interface ZjudgBusinessPaymentMapper
{
    int insert(ZjudgBusinessPayment item);
    ZjudgBusinessPayment selectPending(@Param("bizType") String bizType, @Param("bizId") String bizId, @Param("userId") String userId);
    ZjudgBusinessPayment selectLatest(@Param("bizType") String bizType, @Param("bizId") String bizId, @Param("userId") String userId);
    ZjudgBusinessPayment selectByOutTradeNo(String outTradeNo);
    int updatePrepayId(@Param("outTradeNo") String outTradeNo, @Param("prepayId") String prepayId);
    int markPaid(@Param("outTradeNo") String outTradeNo, @Param("transactionId") String transactionId);
}
