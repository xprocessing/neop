package com.gongziyu.neop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.entity.*;
import com.gongziyu.neop.exception.BusinessException;
import com.gongziyu.neop.exception.LockAcquisitionException;
import com.gongziyu.neop.mapper.*;
import com.gongziyu.neop.service.PointService;
import com.gongziyu.neop.service.ShopService;
import com.gongziyu.neop.util.OrderNoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ProductMapper, Product> implements ShopService {

    @Autowired private ProductMapper productMapper;
    @Autowired private ProductCategoryMapper productCategoryMapper;
    @Autowired private CartMapper cartMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderProductMapper orderProductMapper;
    @Autowired private UserAddressMapper userAddressMapper;
    @Autowired private RedissonClient redissonClient;
    @Autowired private StringRedisTemplate stringRedisTemplate;

    // ===== 商品分类 =====

    @Override
    public List<ProductCategory> categoryTree() {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getStatus, 1).orderByAsc(ProductCategory::getSort);
        return productCategoryMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCategory(ProductCategory category) {
        productCategoryMapper.insert(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(ProductCategory category) {
        productCategoryMapper.updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        productCategoryMapper.deleteById(id);
    }

    // ===== 商品 =====

    @Override
    public IPage<Product> productList(PageDTO pageDTO, Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1).gt(Product::getStock, 0);
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Product::getSort, Product::getCreateTime);
        return productMapper.selectPage(pageDTO.getPage(), wrapper);
    }

    @Override
    public IPage<Product> adminProductList(PageDTO pageDTO, String productName, Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(productName)) {
            wrapper.like(Product::getProductName, productName);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return productMapper.selectPage(pageDTO.getPage(), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProduct(Product product) {
        productMapper.insert(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Product product) {
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }

    @Override
    public void updateProductStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        productMapper.updateById(product);
    }

    // ===== 购物车 =====

    @Override
    public List<Map<String, Object>> cartList(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId).orderByDesc(Cart::getCreateTime);
        List<Cart> carts = cartMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Cart cart : carts) {
            Map<String, Object> item = new HashMap<>();
            item.put("cart", cart);
            Product product = productMapper.selectById(cart.getProductId());
            item.put("product", product);
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(Long userId, Long productId, Integer num) {
        // 查找是否已在购物车
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId).eq(Cart::getProductId, productId);
        Cart existing = cartMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setNum(existing.getNum() + num);
            cartMapper.updateById(existing);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setNum(num);
            cart.setIsChecked(1);
            cartMapper.insert(cart);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCart(Long cartId, Long userId, Integer num, Integer isChecked) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw BusinessException.of(400, "购物车记录不存在");
        }
        if (num != null) {
            cart.setNum(num);
        }
        if (isChecked != null) {
            cart.setIsChecked(isChecked);
        }
        cartMapper.updateById(cart);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCart(String ids, Long userId) {
        List<Long> idList = Arrays.stream(ids.split(",")).map(Long::parseLong).collect(Collectors.toList());
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Cart::getId, idList).eq(Cart::getUserId, userId);
        cartMapper.delete(wrapper);
    }

    // ===== 订单 =====

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 15)
    public Map<String, Object> createOrder(Long userId, Long addressId, String cartIds, Long productId, Integer num, String remark) {
        // 分布式锁防止重复下单
        String lockKey = "neop:lock:order:create:" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 5, TimeUnit.SECONDS);
            if (!locked) {
                throw new LockAcquisitionException("下单操作过于频繁");
            }

            // 1. 获取收货地址
            UserAddress address = userAddressMapper.selectById(addressId);
            if (address == null || !address.getUserId().equals(userId)) {
                throw BusinessException.of(6001, "收货地址不存在");
            }

            // 2. 获取商品列表
            List<Cart> selectedCarts = new ArrayList<>();
            if (StringUtils.isNotBlank(cartIds)) {
                // 从购物车结算
                List<Long> cartIdList = Arrays.stream(cartIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
                selectedCarts = cartMapper.selectBatchIds(cartIdList);
            } else if (productId != null && num != null) {
                // 直接购买
                Cart tempCart = new Cart();
                tempCart.setProductId(productId);
                tempCart.setNum(num);
                selectedCarts.add(tempCart);
            }

            if (selectedCarts.isEmpty()) {
                throw BusinessException.of(400, "请选择商品");
            }

            // 3. 计算总金额 & 扣减库存
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderProduct> orderProducts = new ArrayList<>();

            for (Cart cart : selectedCarts) {
                Product product = productMapper.selectById(cart.getProductId());
                if (product == null || product.getStatus() == 0) {
                    throw BusinessException.of(2001, "商品不存在或已下架");
                }
                if (product.getStock() < cart.getNum()) {
                    throw BusinessException.of(2002, "商品库存不足：" + product.getProductName());
                }

                // 扣减库存（乐观锁）
                int updated = productMapper.deductStock(product.getId(), cart.getNum());
                if (updated == 0) {
                    throw BusinessException.of(2002, "商品库存不足：" + product.getProductName());
                }

                BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cart.getNum()));
                totalAmount = totalAmount.add(subtotal);

                OrderProduct op = new OrderProduct();
                op.setProductId(product.getId());
                op.setProductName(product.getProductName());
                op.setProductImg(product.getProductImg());
                op.setPrice(product.getPrice());
                op.setNum(cart.getNum());
                op.setSubtotalAmount(subtotal);
                orderProducts.add(op);
            }

            // 4. 创建订单
            String orderNo = OrderNoUtil.generateOrderNo();
            Order order = new Order();
            order.setUserId(userId);
            order.setOrderNo(orderNo);
            order.setTotalAmount(totalAmount);
            order.setPayAmount(totalAmount);
            order.setStatus(0);  // 待付款
            order.setAddressId(addressId);
            order.setReceiverName(address.getName());
            order.setReceiverPhone(address.getPhone());
            order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
            order.setRemark(remark != null ? remark : "");
            orderMapper.insert(order);

            // 5. 创建订单明细
            for (OrderProduct op : orderProducts) {
                op.setOrderId(order.getId());
                op.setOrderNo(orderNo);
                op.setUserId(userId);
                orderProductMapper.insert(op);
            }

            // 6. 清空已选购物车
            if (StringUtils.isNotBlank(cartIds)) {
                List<Long> cartIdList = Arrays.stream(cartIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
                cartMapper.deleteBatchIds(cartIdList);
            }

            // 7. 返回订单信息
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getId());
            result.put("orderNo", orderNo);
            result.put("totalAmount", totalAmount);
            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw BusinessException.of("下单失败");
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    public Map<String, Object> payOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw BusinessException.of(5001, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw BusinessException.of(5002, "订单已支付，无需重复支付");
        }

        // TODO: 调用微信统一下单API
        Map<String, Object> payParams = new HashMap<>();
        payParams.put("orderNo", order.getOrderNo());
        payParams.put("amount", order.getPayAmount());
        return payParams;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void handleShopCallback(String orderNo, String wechatPayNo) {
        // 1. Redis幂等校验
        String idempotentKey = "neop:wechat:callback:idempotent:" + orderNo;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(idempotentKey))) {
            return;
        }

        // 2. 查询订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = orderMapper.selectOne(wrapper);
        if (order == null || order.getStatus() != 0) {
            stringRedisTemplate.opsForValue().set(idempotentKey, "1", 24, TimeUnit.HOURS);
            return;
        }

        // 3. 更新订单状态为待发货
        order.setStatus(1);
        order.setPayTime(new Date());
        orderMapper.updateById(order);

        // 4. 设置幂等标记
        stringRedisTemplate.opsForValue().set(idempotentKey, "1", 24, TimeUnit.HOURS);

        log.info("[电商支付回调] 处理成功 orderNo={}", orderNo);
    }

    @Override
    public IPage<Map<String, Object>> myOrderList(Long userId, PageDTO pageDTO, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        IPage<Order> orderPage = orderMapper.selectPage(pageDTO.getPage(), wrapper);
        // 转换为Map分页
        return orderPage.convert(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("order", order);
            // 查询订单明细
            LambdaQueryWrapper<OrderProduct> opWrapper = new LambdaQueryWrapper<>();
            opWrapper.eq(OrderProduct::getOrderId, order.getId());
            map.put("products", orderProductMapper.selectList(opWrapper));
            return map;
        });
    }

    @Override
    public Map<String, Object> orderInfo(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw BusinessException.of(5001, "订单不存在");
        }
        LambdaQueryWrapper<OrderProduct> opWrapper = new LambdaQueryWrapper<>();
        opWrapper.eq(OrderProduct::getOrderId, orderId);
        List<OrderProduct> products = orderProductMapper.selectList(opWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("products", products);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw BusinessException.of(5001, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw BusinessException.of(400, "仅待付款订单可取消");
        }

        // 更新状态为已取消
        order.setStatus(4);
        order.setCancelTime(new Date());
        orderMapper.updateById(order);

        // 释放库存
        LambdaQueryWrapper<OrderProduct> opWrapper = new LambdaQueryWrapper<>();
        opWrapper.eq(OrderProduct::getOrderId, orderId);
        List<OrderProduct> products = orderProductMapper.selectList(opWrapper);
        for (OrderProduct op : products) {
            productMapper.addStock(op.getProductId(), op.getNum());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw BusinessException.of(5001, "订单不存在");
        }
        if (order.getStatus() != 2) {
            throw BusinessException.of(400, "仅待收货订单可确认收货");
        }
        order.setStatus(3);
        order.setFinishTime(new Date());
        orderMapper.updateById(order);
    }

    @Override
    public IPage<Map<String, Object>> adminOrderList(PageDTO pageDTO, String orderNo, Integer status, String startDate, String endDate) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(orderNo)) {
            wrapper.eq(Order::getOrderNo, orderNo);
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (StringUtils.isNotBlank(startDate)) {
            wrapper.ge(Order::getCreateTime, startDate + " 00:00:00");
        }
        if (StringUtils.isNotBlank(endDate)) {
            wrapper.le(Order::getCreateTime, endDate + " 23:59:59");
        }
        wrapper.orderByDesc(Order::getCreateTime);
        IPage<Order> orderPage = orderMapper.selectPage(pageDTO.getPage(), wrapper);
        return orderPage.convert(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("order", order);
            LambdaQueryWrapper<OrderProduct> opWrapper = new LambdaQueryWrapper<>();
            opWrapper.eq(OrderProduct::getOrderId, order.getId());
            map.put("products", orderProductMapper.selectList(opWrapper));
            return map;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 1) {
            throw BusinessException.of(400, "仅待发货订单可发货");
        }
        order.setStatus(2);
        order.setSendTime(new Date());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminCancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw BusinessException.of(5001, "订单不存在");
        }
        order.setStatus(4);
        order.setCancelTime(new Date());
        orderMapper.updateById(order);

        // 释放库存
        LambdaQueryWrapper<OrderProduct> opWrapper = new LambdaQueryWrapper<>();
        opWrapper.eq(OrderProduct::getOrderId, orderId);
        List<OrderProduct> products = orderProductMapper.selectList(opWrapper);
        for (OrderProduct op : products) {
            productMapper.addStock(op.getProductId(), op.getNum());
        }
    }
}
