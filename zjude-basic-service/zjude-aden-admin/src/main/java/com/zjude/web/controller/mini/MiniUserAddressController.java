package com.zjude.web.controller.mini;

import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zjude.common.core.controller.BaseController;
import com.zjude.common.core.domain.AjaxResult;
import com.zjude.common.utils.SecurityUtils;
import com.zjude.system.domain.ZjudgUserAddress;
import com.zjude.system.service.IZjudgUserAddressService;

/**
 * 小程序用户地址簿接口。
 *
 * <p>用户身份一律取登录态，不接受客户端传入 userId，
 * 因此不存在通过改请求体读写他人地址的路径。</p>
 */
@RestController
@RequestMapping("/api/mini/user/addresses")
public class MiniUserAddressController extends BaseController
{
    @Resource
    private IZjudgUserAddressService addressService;

    /** 我的地址列表，默认地址排在最前。 */
    @GetMapping
    public AjaxResult list()
    {
        List<ZjudgUserAddress> rows = addressService.selectByUser(SecurityUtils.getLoginUser().getUserId());
        return success(rows);
    }

    /** 默认地址，没有地址时 data 为 null。 */
    @GetMapping("/default")
    public AjaxResult getDefault()
    {
        return success(addressService.selectDefault(SecurityUtils.getLoginUser().getUserId()));
    }

    /** 地址详情。 */
    @GetMapping("/{addressId}")
    public AjaxResult get(@PathVariable String addressId)
    {
        return success(addressService.selectById(SecurityUtils.getLoginUser().getUserId(), addressId));
    }

    /** 新增地址，用户的第一条地址自动成为默认。 */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ZjudgUserAddress address)
    {
        return success(addressService.add(SecurityUtils.getLoginUser().getUserId(), address));
    }

    /** 修改地址。 */
    @PutMapping("/{addressId}")
    public AjaxResult edit(@PathVariable String addressId, @Validated @RequestBody ZjudgUserAddress address)
    {
        address.setAddressId(addressId);
        return toAjax(addressService.update(SecurityUtils.getLoginUser().getUserId(), address));
    }

    /** 设为默认地址。 */
    @PutMapping("/{addressId}/default")
    public AjaxResult setDefault(@PathVariable String addressId)
    {
        return toAjax(addressService.setDefault(SecurityUtils.getLoginUser().getUserId(), addressId));
    }

    /** 删除地址。 */
    @DeleteMapping("/{addressId}")
    public AjaxResult remove(@PathVariable String addressId)
    {
        return toAjax(addressService.delete(SecurityUtils.getLoginUser().getUserId(), addressId));
    }
}
