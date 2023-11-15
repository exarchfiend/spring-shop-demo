package fun.mjauto.shop.controller;

import fun.mjauto.shop.pojo.dto.ApiResponse;
import fun.mjauto.shop.service.VoucherOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author MJ
 * @description 优惠券订单API
 * @date 2023/11/15
 */
@RestController
@RequestMapping("/order")
public class VoucherOrderController {
    private final VoucherOrderService voucherOrderService;

    @Autowired
    public VoucherOrderController(VoucherOrderService voucherOrderService) {
        this.voucherOrderService = voucherOrderService;
    }

    /**
     * 根据优惠券ID创建优惠券订单
     *
     * @param id  优惠券ID
     * @return 订单ID
     */
    @PostMapping("/create")
    public ApiResponse<?> createOrder(@RequestParam("id") Long id) {
        return voucherOrderService.createVoucherOrder(id);
    }
}
