package com.zjude.web.controller.zjudg;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.system.domain.ZjudgMasterWalletLog;
import com.zjude.system.service.IZjudgSettleService;

/**
 * 结算流水后台接口。
 */
@RestController
@RequestMapping("/zjudg/settle")
public class ZjudgSettleController extends BaseController
{
    @Resource
    private IZjudgSettleService settleService;

    /** 结算流水列表 */
    @PreAuthorize("@ss.hasPerm('zjudg:settle:list')")
    @GetMapping("/list")
    public TableDataInfo list(ZjudgMasterWalletLog query)
    {
        startPage();
        List<ZjudgMasterWalletLog> rows = settleService.listAllLogs(query);
        return getDataTable(rows);
    }
}
