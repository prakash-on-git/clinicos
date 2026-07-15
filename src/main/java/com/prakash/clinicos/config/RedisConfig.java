package com.prakash.clinicos.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis-backed caching for expensive, read-heavy computations.
 *
 * Why cache doctor availability specifically?
 * computeAvailability() re-derives every 10-minute slot for a day from five
 * different tables (schedule, breaks, overrides, leave, booked appointments)
 * on every request. Under concurrent booking-widget traffic the same
 * (doctorId, date) pair is requested repeatedly within seconds.
 *
 * Why both a short TTL and explicit @CacheEvict calls on writes?
 * Booking/cancelling/rescheduling evicts the exact (doctorId, date) entry
 * immediately — a slot must never appear free after it's been taken.
 * Schedule/override/leave writes evict the whole cache, since one change
 * can affect many future dates. The 30s TTL is just a backstop in case a
 * write path is ever added that forgets to evict.
 *
 * Why JDK serialization instead of JSON for the cached value?
 * DoctorAvailabilityResponse is a Lombok @Builder-only class with no default
 * constructor, so Jackson can't reconstruct it from JSON without a hand-built
 * builder deserializer. Making it Serializable and using plain Java
 * serialization here avoids that entirely — this is an internal cache, not a
 * public API contract, so JDK's binary format is a fine trade for simplicity.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    public static final String DOCTOR_AVAILABILITY_CACHE = "doctorAvailability";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration availabilityConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new JdkSerializationRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration(DOCTOR_AVAILABILITY_CACHE, availabilityConfig)
                .enableStatistics() // otherwise cache_gets_total hit/miss stay at zero in /actuator/prometheus
                .build();
    }
}
