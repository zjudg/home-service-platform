package com.zjude.system.service;

import java.util.List;
import com.zjude.system.domain.ZjudgOrderExtraTime;
import com.zjude.system.domain.ZjudgService;

public interface IZjudgOrderExtraTimeService
{
    ZjudgService getProductForOrder(String orderId, String userId);
    List<ZjudgOrderExtraTime> listForOrder(String orderId, String userId);
    ZjudgOrderExtraTime create(String orderId, String userId, int hours);
    ZjudgOrderExtraTime getForUser(String extraTimeId, String userId);
    boolean markPaidAndSettle(String extraTimeId);
}
