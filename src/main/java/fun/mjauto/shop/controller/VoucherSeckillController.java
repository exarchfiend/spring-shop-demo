package fun.mjauto.shop.controller;

import fun.mjauto.shop.pojo.dto.ApiResponse;
import fun.mjauto.shop.service.VoucherOrderService;
import fun.mjauto.shop.service.VoucherSeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author MJ
 * @description 秒杀优惠券API
 * @date 2023/11/15
 */
@RestController
@RequestMapping("/voucherSeckill")
public class VoucherSeckillController {
    private final VoucherSeckillService voucherSeckillService;

    @Autowired
    public VoucherSeckillController(VoucherSeckillService voucherSeckillService) {
        this.voucherSeckillService = voucherSeckillService;
    }

    /**
     * 根据优惠券ID创建Redis缓存
     *
     * @param id  优惠券ID
     * @return 订单ID
     */
    @PostMapping("/setCache/admin")
    public ApiResponse<?> createOrder(@RequestParam("id") Long id) {
        return voucherSeckillService.setVoucherSeckillCache(id);
    }
}
