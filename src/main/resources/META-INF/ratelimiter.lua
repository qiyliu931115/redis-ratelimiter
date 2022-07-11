local rateLimitKey = KEYS[1];
local rate = tonumber(KEYS[2]);
local rateInterval = tonumber(KEYS[3]);
local limitResult = 0;
local ttlResult = 0;
local currValue = redis.call('incr', rateLimitKey);
if (currValue == 1) then
    redis.call('expire', rateLimitKey, rateInterval);
    limitResult = 0;
else
    if (currValue > rate) then
        limitResult = 1;
        ttlResult = redis.call('ttl', rateLimitKey);
    end
end
return { limitResult, ttlResult }