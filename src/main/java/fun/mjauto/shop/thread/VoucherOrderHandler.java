package fun.mjauto.shop.thread;

import cn.hutool.core.bean.BeanUtil;
import fun.mjauto.shop.pojo.entity.VoucherOrderEntity;
import fun.mjauto.shop.service.VoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author MJ
 * @description
 * @date 2023/11/15
 */
@Slf4j
@Component
public class VoucherOrderHandler implements Runnable {
    private final StringRedisTemplate stringRedisTemplate;
    private final VoucherOrderService voucherOrderService;

    public VoucherOrderHandler(StringRedisTemplate stringRedisTemplate, VoucherOrderService voucherOrderService1) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.voucherOrderService = voucherOrderService1;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // 1.获取消息队列中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS s1 >
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from("g1", "c1"),
                        StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                        StreamOffset.create("stream.orders", ReadOffset.lastConsumed())
                );
                // 2.判断订单信息是否为空
                if (list == null || list.isEmpty()) {
                    // 如果为null，说明没有消息，继续下一次循环
                    continue;
                }
                // 解析数据
                MapRecord<String, Object, Object> record = list.get(0);
                Map<Object, Object> value = record.getValue();
                VoucherOrderEntity voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrderEntity(), true);
                // 3.创建订单
                boolean flag = voucherOrderService.createVoucherOrder(voucherOrder);
                // 4.确认消息 XACK
                stringRedisTemplate.opsForStream().acknowledge("s1", "g1", record.getId());
            } catch (Exception e) {
                log.error("处理订单异常", e);
                handlePendingList();
            }
        }
    }

    private void handlePendingList() {
        while (true) {
            try {
                // 1.获取pending-list中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS s1 0
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from("g1", "c1"),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create("stream.orders", ReadOffset.from("0"))
                );
                // 2.判断订单信息是否为空
                if (list == null || list.isEmpty()) {
                    // 如果为null，说明没有异常消息，结束循环
                    break;
                }
                // 解析数据
                MapRecord<String, Object, Object> record = list.get(0);
                Map<Object, Object> value = record.getValue();
                VoucherOrderEntity voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrderEntity(), true);
                // 3.创建订单
                boolean flag = voucherOrderService.createVoucherOrder(voucherOrder);
                // 4.确认消息 XACK
                stringRedisTemplate.opsForStream().acknowledge("s1", "g1", record.getId());
            } catch (Exception e) {
                log.error("处理订单异常", e);
            }
        }
    }
}
