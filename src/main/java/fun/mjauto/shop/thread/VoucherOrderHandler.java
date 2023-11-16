package fun.mjauto.shop.thread;

import cn.hutool.core.bean.BeanUtil;
import fun.mjauto.shop.pojo.entity.VoucherOrderEntity;
import fun.mjauto.shop.service.VoucherOrderService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static fun.mjauto.shop.constant.RedisConstants.*;

/**
 * @author MJ
 * @description 优惠券订单消费者
 * @date 2023/11/15
 */
@Slf4j
@Component
public class VoucherOrderHandler implements Runnable {
    private final StringRedisTemplate stringRedisTemplate;
    private final VoucherOrderService voucherOrderService;
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    public VoucherOrderHandler(StringRedisTemplate stringRedisTemplate, VoucherOrderService voucherOrderService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.voucherOrderService = voucherOrderService;
    }

    @PostConstruct
    private void init() {
        try {
            // 检查消费组是否存在
            stringRedisTemplate.opsForStream().createGroup(SECKILL_STREAM_KEY,ReadOffset.from("0"), SECKILL_STREAM_GROUP_NAME);
        } catch (Exception e) {
            // 消费组已经存在，可以忽略异常
            log.error("消费者组已经存在");
        }
        SECKILL_ORDER_EXECUTOR.submit(this);
    }

    @Override
    public void run() {
        log.info("秒杀优惠券订单消费者consumer1开始工作...");
        while (true) {
            try {
                // 获取消息队列中的订单信息
                @SuppressWarnings("unchecked")
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from(SECKILL_STREAM_GROUP_NAME, "consumer1"),
                        StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                        StreamOffset.create(SECKILL_STREAM_KEY, ReadOffset.lastConsumed())
                );
                // 判断订单信息是否为空
                if (list == null || list.isEmpty()) {
                    // 如果为null，说明没有消息，继续下一次循环
                    continue;
                }
                // 处理订单信息
                creatOrder(list.get(0));
            } catch (Exception e) {
                log.error("处理订单异常", e);
                handlePendingList();
            }
        }
    }

    private void handlePendingList() {
        while (true) {
            try {
                // 获取pending-list中的订单信息
                @SuppressWarnings("unchecked")
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from(SECKILL_STREAM_GROUP_NAME, "consumer1"),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(SECKILL_STREAM_KEY, ReadOffset.from("0"))
                );
                // 判断订单信息是否为空
                if (list == null || list.isEmpty()) {
                    // 如果为null，说明没有异常消息，结束循环
                    break;
                }
                // 处理订单信息
                creatOrder(list.get(0));
            } catch (Exception e) {
                log.error("处理订单异常", e);
            }
        }
    }

    private void creatOrder(MapRecord<String, Object, Object> record){
        // 解析数据
        Map<Object, Object> value = record.getValue();
        VoucherOrderEntity voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrderEntity(), true);
        // 创建订单
        boolean flag = voucherOrderService.createVoucherOrder(voucherOrder);
        // 确认消息
        stringRedisTemplate.opsForStream().acknowledge(SECKILL_STREAM_KEY, SECKILL_STREAM_GROUP_NAME, record.getId());
    }
}
