package com.gdg.z_meet.domain.order.infrastructure;

import feign.RequestTemplate;

@FunctionalInterface
public interface RequestInterceptor {
    void apply(RequestTemplate template);     // OpenFeign

}
