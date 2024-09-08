package com.polarbookshop.orderservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@Configuration
@EnableR2dbcAuditing // 지속성 엔티티에 대한 R2DBC 감사를 활성화
public class DataConfig {
}
