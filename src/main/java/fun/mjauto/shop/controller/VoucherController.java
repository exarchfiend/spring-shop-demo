package fun.mjauto.shop.controller;

import fun.mjauto.shop.pojo.dto.ApiResponse;
import fun.mjauto.shop.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author MJ
 * @description 优惠券API
 * @date 2023/11/15
 */
@RestController
@RequestMapping("/voucher")
public class VoucherController {
    private final VoucherService voucherService;

    @Autowired
    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    /**
     * 根据优惠券ID查询优惠券
     *
     * @param id  优惠券ID
     * @return 优惠券
     */
    @PostMapping("/select/id")
    public ApiResponse<?> selectVoucherById(@RequestParam("id") Long id){
        return voucherService.selectVoucherById(id);
    }
}
