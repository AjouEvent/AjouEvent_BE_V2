-- KEYS[1]: dedupKey  (ClubEvent:dedup:{eventId}:u:{email} or :a:{hash})
-- KEYS[2]: viewKey   (ClubEvent:views:{eventId})
-- KEYS[3]: dirtyKey  (ClubEvent:dirty)
-- ARGV[1]: dedup TTL in seconds
-- ARGV[2]: eventId   (string, for dirty set member)
-- ARGV[3]: viewKey safety TTL in seconds
-- Returns: 1 if view counted, 0 if duplicate
local isNew = redis.call('SET', KEYS[1], '1', 'NX', 'EX', tonumber(ARGV[1]))
if isNew then
    redis.call('INCR', KEYS[2])
    redis.call('SADD', KEYS[3], ARGV[2])
    redis.call('EXPIRE', KEYS[2], tonumber(ARGV[3]))
    return 1
end
return 0
