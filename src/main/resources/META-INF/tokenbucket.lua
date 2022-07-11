redis.replicate_commands()

local key = KEYS[1] -- 令牌桶标识
local capacity = tonumber(KEYS[2]) -- 最大容量
local quota = tonumber(KEYS[3]) -- 时间窗口内的限额
local period = tonumber(KEYS[4]) -- 时间窗口大小（秒）
local quantity = tonumber(KEYS[5]) or 1 -- 需要的令牌数量，默认为1
local timestamp = tonumber(redis.call('time')[1]) -- 当前时间戳

assert(type(capacity) == "number", "capacity is not a number!")
assert(type(quota) == "number", "quota is not a number!")
assert(type(period) == "number", "period is not a number!")
assert(type(quantity) == "number", "quantity is not a number!")

-- 第一次请求时创建令牌桶
if (redis.call('exists', key) == 0) then
    redis.call('hmset', key, 'remain', capacity, 'timestamp', timestamp)
else
    -- 计算从上次生成到现在这段时间应该生成的令牌数
    local remain = tonumber(redis.call('hget', key, 'remain'))
    local last_reset = tonumber(redis.call('hget', key, 'timestamp'))
    local delta_quota = math.floor(((timestamp - last_reset) / period) * quota)
    if (delta_quota > 0) then
        remain = remain + delta_quota
        if (remain > capacity) then
            remain = capacity
        end
        redis.call('hmset', key, 'remain', remain, 'timestamp', timestamp)
    end
end

-- 支持动态调整容量和令牌生成速率
redis.call('hmset', key, 'capacity', capacity, 'quota', quota, 'period', period);

local result = {} -- 返回的结果集
local remain = tonumber(redis.call('hget', key, 'remain'))
if (remain < quantity) then
    result = {1, capacity, remain}
else
    result = {0, capacity, remain - quantity}
    redis.call('hincrby', key, 'remain', -quantity)
end

return result