package com.gongziyu.neop.controller.trade;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gongziyu.neop.annotation.OperationLog;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.common.Result;
import com.gongziyu.neop.entity.Product;
import com.gongziyu.neop.entity.ProductCategory;
import com.gongziyu.neop.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 电商模块接口
 */
@RestController
public class ShopController {

    @Autowired
    private ShopService shopService;

    // ===== 前台：商品分类 =====

    @GetMapping("/api/shop/category/list")
    public Result<List<ProductCategory>> categoryList() {
        return Result.success(shopService.categoryTree());
    }

    // ===== 前台：商品 =====

    @GetMapping("/api/shop/product/list")
    public Result<IPage<Product>> productList(PageDTO pageDTO,
                                                @RequestParam(required = false) Long categoryId) {
        return Result.success(shopService.productList(pageDTO, categoryId));
    }

    @GetMapping("/api/shop/product/info/{id}")
    public Result<Product> productInfo(@PathVariable Long id) {
        Product product = shopService.getById(id);
        if (product == null || product.getStatus() == 0) {
            return Result.error(2001, "商品不存在或已下架");
        }
        return Result.success(product);
    }

    // ===== 前台：购物车 =====

    @GetMapping("/api/shop/cart/list")
    public Result<List<Map<String, Object>>> cartList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(shopService.cartList(userId));
    }

    @PostMapping("/api/shop/cart/add")
    public Result<Void> addToCart(@RequestBody CartAddDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        shopService.addToCart(userId, dto.getProductId(), dto.getNum());
        return Result.success();
    }

    @PostMapping("/api/shop/cart/update")
    public Result<Void> updateCart(@RequestBody CartUpdateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        shopService.updateCart(dto.getId(), userId, dto.getNum(), dto.getIsChecked());
        return Result.success();
    }

    @PostMapping("/api/shop/cart/delete")
    public Result<Void> deleteCart(@RequestBody CartDeleteDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        shopService.deleteCart(dto.getIds(), userId);
        return Result.success();
    }

    // ===== 前台：订单 =====

    @PostMapping("/api/shop/order/create")
    public Result<Map<String, Object>> createOrder(@RequestBody OrderCreateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = shopService.createOrder(userId, dto.getAddressId(), dto.getCartIds(), dto.getProductId(), dto.getNum(), dto.getRemark());
        return Result.success(result);
    }

    @PostMapping("/api/shop/order/pay")
    public Result<Map<String, Object>> payOrder(@RequestBody OrderPayDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = shopService.payOrder(userId, dto.getOrderId());
        return Result.success(result);
    }

    @GetMapping("/api/shop/order/my/list")
    public Result<IPage<Map<String, Object>>> myOrderList(PageDTO pageDTO,
                                                           @RequestParam(required = false) Integer status,
                                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(shopService.myOrderList(userId, pageDTO, status));
    }

    @GetMapping("/api/shop/order/info/{id}")
    public Result<Map<String, Object>> orderInfo(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(shopService.orderInfo(id, userId));
    }

    @PostMapping("/api/shop/order/cancel")
    public Result<Void> cancelOrder(@RequestBody OrderIdDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        shopService.cancelOrder(userId, dto.getOrderId());
        return Result.success();
    }

    @PostMapping("/api/shop/order/confirm")
    public Result<Void> confirmOrder(@RequestBody OrderIdDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        shopService.confirmOrder(userId, dto.getOrderId());
        return Result.success();
    }

    // ===== 后台：商品管理 =====

    @GetMapping("/api/admin/shop/goods/list")
    @OperationLog(module = "电商管理", description = "查询商品列表")
    public Result<IPage<Product>> adminGoodsList(PageDTO pageDTO,
                                                   @RequestParam(required = false) String productName,
                                                   @RequestParam(required = false) Integer status) {
        return Result.success(shopService.adminProductList(pageDTO, productName, status));
    }

    @PostMapping("/api/admin/shop/goods/save")
    @OperationLog(module = "电商管理", description = "新增商品")
    public Result<Void> saveGoods(@Valid @RequestBody Product product) {
        shopService.saveProduct(product);
        return Result.success();
    }

    @PostMapping("/api/admin/shop/goods/update")
    @OperationLog(module = "电商管理", description = "编辑商品")
    public Result<Void> updateGoods(@Valid @RequestBody Product product) {
        shopService.updateProduct(product);
        return Result.success();
    }

    @PostMapping("/api/admin/shop/goods/delete")
    @OperationLog(module = "电商管理", description = "删除商品")
    public Result<Void> deleteGoods(@RequestParam Long id) {
        shopService.deleteProduct(id);
        return Result.success();
    }

    @PostMapping("/api/admin/shop/goods/status")
    @OperationLog(module = "电商管理", description = "商品上下架")
    public Result<Void> goodsStatus(@RequestParam Long id, @RequestParam Integer status) {
        shopService.updateProductStatus(id, status);
        return Result.success();
    }

    // ===== 后台：分类管理 =====

    @GetMapping("/api/admin/shop/category/tree")
    public Result<List<ProductCategory>> categoryTree() {
        return Result.success(shopService.categoryTree());
    }

    @PostMapping("/api/admin/shop/category/save")
    @OperationLog(module = "电商管理", description = "新增分类")
    public Result<Void> saveCategory(@RequestBody ProductCategory category) {
        shopService.saveCategory(category);
        return Result.success();
    }

    @PostMapping("/api/admin/shop/category/update")
    @OperationLog(module = "电商管理", description = "编辑分类")
    public Result<Void> updateCategory(@RequestBody ProductCategory category) {
        shopService.updateCategory(category);
        return Result.success();
    }

    @PostMapping("/api/admin/shop/category/delete")
    @OperationLog(module = "电商管理", description = "删除分类")
    public Result<Void> deleteCategory(@RequestParam Long id) {
        shopService.deleteCategory(id);
        return Result.success();
    }

    // ===== 后台：订单管理 =====

    @GetMapping("/api/admin/shop/order/list")
    @OperationLog(module = "电商管理", description = "查询订单列表")
    public Result<IPage<Map<String, Object>>> adminOrderList(PageDTO pageDTO,
                                                               @RequestParam(required = false) String orderNo,
                                                               @RequestParam(required = false) Integer status,
                                                               @RequestParam(required = false) String startDate,
                                                               @RequestParam(required = false) String endDate) {
        return Result.success(shopService.adminOrderList(pageDTO, orderNo, status, startDate, endDate));
    }

    @PostMapping("/api/admin/shop/order/send")
    @OperationLog(module = "电商管理", description = "订单发货")
    public Result<Void> sendOrder(@RequestParam Long orderId) {
        shopService.sendOrder(orderId);
        return Result.success();
    }

    @PostMapping("/api/admin/shop/order/cancel")
    @OperationLog(module = "电商管理", description = "后台取消订单")
    public Result<Void> adminCancelOrder(@RequestParam Long orderId) {
        shopService.adminCancelOrder(orderId);
        return Result.success();
    }

    // ===== DTO =====

    @Data
    public static class CartAddDTO {
        @NotNull(message = "商品ID不能为空") private Long productId;
        @NotNull(message = "数量不能为空") @Min(1) private Integer num;
    }

    @Data
    public static class CartUpdateDTO {
        @NotNull(message = "ID不能为空") private Long id;
        private Integer num;
        private Integer isChecked;
    }

    @Data
    public static class CartDeleteDTO {
        @NotBlank(message = "ids不能为空") private String ids;
    }

    @Data
    public static class OrderCreateDTO {
        @NotNull(message = "地址ID不能为空") private Long addressId;
        private String cartIds;
        private Long productId;
        private Integer num;
        private String remark;
    }

    @Data
    public static class OrderPayDTO {
        @NotNull(message = "订单ID不能为空") private Long orderId;
    }

    @Data
    public static class OrderIdDTO {
        @NotNull(message = "订单ID不能为空") private Long orderId;
    }
}
