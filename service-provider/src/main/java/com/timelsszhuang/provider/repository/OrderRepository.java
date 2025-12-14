package com.timelsszhuang.provider.repository;

import com.timelsszhuang.provider.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 订单 Repository
 *
 * @author timelsszhuang
 */
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    /**
     * 根据订单编号查询订单
     */
    Optional<OrderEntity> findByOrderNo(String orderNo);
}
