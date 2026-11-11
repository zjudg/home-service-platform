package com.zjude.web.controller.zjudg;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.annotation.Log;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.core.page.TableDataInfo;
import com.zjude.common.enums.BusinessType;
import com.zjude.system.domain.ZjudgCertificateProduct;
import com.zjude.system.service.IZjudgCertificateProductService;

@RestController
@RequestMapping("/zjudg/certificate/product")
public class ZjudgCertificateProductController extends BaseController
{
    @Resource private IZjudgCertificateProductService service;

    @PreAuthorize("@ss.hasPerm('zjudg:certificate-product:list') and @ss.hasRole('admin')") @GetMapping("/list")
    public TableDataInfo list(ZjudgCertificateProduct query) { startPage(); List<ZjudgCertificateProduct> rows = service.selectList(query); return getDataTable(rows); }
    @PreAuthorize("@ss.hasPerm('zjudg:certificate-product:query') and @ss.hasRole('admin')") @GetMapping("/{id}")
    public AjaxResult get(@PathVariable String id) { return success(service.selectById(id)); }
    @PreAuthorize("@ss.hasPerm('zjudg:certificate-product:add') and @ss.hasRole('admin')") @Log(title="技能证书商品", businessType=BusinessType.INSERT) @PostMapping
    public AjaxResult add(@Validated @RequestBody ZjudgCertificateProduct item) { item.setCreateBy(getUsername()); return toAjax(service.insert(item)); }
    @PreAuthorize("@ss.hasPerm('zjudg:certificate-product:edit') and @ss.hasRole('admin')") @Log(title="技能证书商品", businessType=BusinessType.UPDATE) @PutMapping
    public AjaxResult edit(@Validated @RequestBody ZjudgCertificateProduct item) { item.setUpdateBy(getUsername()); return toAjax(service.update(item)); }
    @PreAuthorize("@ss.hasPerm('zjudg:certificate-product:remove') and @ss.hasRole('admin')") @Log(title="技能证书商品", businessType=BusinessType.DELETE) @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) { return toAjax(service.deleteByIds(ids)); }
}
