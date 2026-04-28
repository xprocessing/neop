package com.gongziyu.neop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // 登录接口
                        "/api/admin/login",
                        "/api/user/wechat/login",
                        // 微信支付回调（无需Token）
                        "/api/wechat/**",
                        // 公开查询接口
                        "/api/task/list",
                        "/api/task/info/**",
                        "/api/member/package/list",
                        "/api/shop/category/list",
                        "/api/shop/product/list",
                        "/api/shop/product/info/**",
                        // 文件上传（通过其他方式校验）
                        "/api/upload/**",
                        // 健康检查
                        "/actuator/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 本地文件上传映射（开发环境）
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:/data/neop/upload/");
    }
}
