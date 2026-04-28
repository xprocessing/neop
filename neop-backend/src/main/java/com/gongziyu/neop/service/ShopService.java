package com.gongziyu.neop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.entity.Product;
import com.gongziyu.neop.entity.ProductCategory;

import java.util.List;
import java.util.Map;

public interface ShopService extends IService<Product> {

    // 商品分类
    List<ProductCategory> categoryTree();
    void saveCategory(ProductCategory category);
    void updateCategory(ProductCategory category);
    void deleteCategory(Long id);

    // 商品
    IPage<Product> productList(PageDTO pageDTO, Long categoryId);
    IPage<Product> adminProductList(PageDTO pageDTO, String productName, Integer status);
    void saveProduct(Product product);
    void updateProduct(Product product);
    void deleteProduct(Long id);
    void updateProductStatus(Long id, Integer status);

    // 购物车
    List<Map<String, Object>> cartList(Long userId);
    void addToCart(Long userId, Long productId, Integer num);
    void updateCart(Long cartId, Long userId, Integer num, Integer isChecked);
    void deleteCart(String ids, Long userId);

    // 订单
    Map<String, Object> createOrder(Long userId, Long addressId, String cartIds, Long productId, Integer num, String remark);
    Map<String, Object> payOrder(Long userId, Long orderId);
    void handleShopCallback(String orderNo, String wechatPayNo);
    IPage<Map<String, Object>> myOrderList(Long userId, PageDTO pageDTO, Integer status);
    Map<String, Object> orderInfo(Long orderId, Long userId);
    void cancelOrder(Long userId, Long orderId);
    void confirmOrder(Long userId, Long orderId);

    // 后台订单
    IPage<Map<String, Object>> adminOrderList(PageDTO pageDTO, String orderNo, Integer status, String startDate, String endDate);
    void sendOrder(Long orderId);
    void adminCancelOrder(Long orderId);
}
