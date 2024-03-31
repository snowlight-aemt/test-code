package me.snowlight.springdistribute.config.database;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sharding")
@Setter
public class FriendShardingConfig {
    private ShardingProperty friend;

    @PostConstruct
    public void init() {
        ShardingConfig.getShardingPropertyMap().put(ShardingTarget.FRIEND, friend);
    }
}