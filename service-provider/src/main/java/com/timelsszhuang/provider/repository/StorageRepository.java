package com.timelsszhuang.provider.repository;

import com.timelsszhuang.provider.entity.StorageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 库存 Repository
 *
 * @author timelsszhuang
 */
@Repository
public interface StorageRepository extends JpaRepository<StorageEntity, Long> {

    /**
     * 根据商品编码查询库存
     */
    Optional<StorageEntity> findByCommodityCode(String commodityCode);

    /**
     * 扣减库存
     */
    @Modifying
    @Query("UPDATE StorageEntity s SET s.used = s.used + :count, s.residue = s.residue - :count WHERE s.commodityCode = :commodityCode AND s.residue >= :count")
    int deductStorage(@Param("commodityCode") String commodityCode, @Param("count") Integer count);
}
