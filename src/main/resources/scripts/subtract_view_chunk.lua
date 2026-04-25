-- Processes a chunk of view count subtractions atomically.
-- KEYS[1..n]  : view count keys (ClubEvent:views:{eventId})
-- KEYS[n+1]   : dirty set key   (ClubEvent:dirty)
-- ARGV[i*2-1] : delta   for entry i  (amount to subtract)
-- ARGV[i*2]   : eventId for entry i  (for dirty set removal)
-- n = #KEYS - 1
-- Returns: 1 on success
local n = #KEYS - 1
local dirtyKey = KEYS[n + 1]
for i = 1, n do
    local remaining = redis.call('DECRBY', KEYS[i], tonumber(ARGV[(i - 1) * 2 + 1]))
    if remaining <= 0 then
        redis.call('DEL', KEYS[i])
        redis.call('SREM', dirtyKey, ARGV[(i - 1) * 2 + 2])
    end
end
return 1
