-- 1.参数列表
-- 1.1.优惠券id
local voucherId = ARGV[1]
-- 1.2.用户id
local userId = ARGV[2]
-- 1.3.订单id
local orderId = ARGV[3]
-- 1.4.获取当前时间
local currentTime = ARGV[4]

-- 2.全局常量
-- 2.1.优惠券key 'shop:seckill:stock:'
local voucherKey = ARGV[5] .. voucherId
-- 2.2.订单key 'shop:seckill:order:'
local orderKey = ARGV[6] .. voucherId
-- 2.2.订单key 'shop:stream.orders'
local streamName = ARGV[7]

-- 3.脚本业务
-- 3.1.判断库存是否充足 get voucherKey
-- 检查库存和有效期
if (redis.call('HGET', voucherKey, 'beginTime')) > currentTime then
    -- 活动尚未开始
    return 1
end
if (redis.call('HGET', voucherKey, 'endTime')) < currentTime then
    -- 活动已经结束
    return 2
end
if (redis.call('HGET', voucherKey, 'stock')) < '1' then
    -- 库存不足
    return 3
end

-- 3.2.判断用户是否下单 SISMEMBER orderKey userId
if (redis.call('SISMEMBER', orderKey, userId) == 1) then
    -- 3.3.存在，说明是重复下单，返回2
    return 4
end
-- 3.4.扣库存 HINCRBY KEY_NAME FIELD_NAME INCR_BY_NUMBER
redis.call('HINCRBY', voucherKey, 'stock', -1)
-- 3.5.下单（保存用户）SADD orderKey userId
redis.call('SADD', orderKey, userId)
-- 3.6.发送消息到队列中， XADD stream.orders * k1 v1 k2 v2 ...
redis.call('XADD', streamName, '*', 'userId', userId, 'voucherId', voucherId, 'id', orderId)
return 0
