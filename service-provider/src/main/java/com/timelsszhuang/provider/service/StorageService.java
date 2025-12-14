package com.timelsszhuang.provider.service;

import com.timelsszhuang.provider.entity.StorageEntity;
import com.timelsszhuang.provider.repository.StorageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存服务 (Seata Demo)
 *
 * @author timelsszhuang
 */
@Service
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

    @Autowired
    private StorageRepository storageRepository;

    /**
     * 扣减库存
     *
     * @param commodityCode 商品编码
     * @param count         扣减数量
     */
    @Transactional
    public void deductStorage(String commodityCode, Integer count) {
        logger.info("==> 开始扣减库存: commodityCode={}, count={}", commodityCode, count);

        // 查询库存
        StorageEntity storage = storageRepository.findByCommodityCode(commodityCode)
                .orElseThrow(() -> new RuntimeException("商品不存在: " + commodityCode));

        // 检查库存是否充足
        if (storage.getResidue() < count) {
            throw new RuntimeException("库存不足! 当前库存: " + storage.getResidue() + ", 需要: " + count);
        }

        // 扣减库存
        int updated = storageRepository.deductStorage(commodityCode, count);
        if (updated == 0) {
            throw new RuntimeException("扣减库存失败，请稍后重试");
        }

        logger.info("==> 库存扣减成功: commodityCode={}, 扣减数量={}", commodityCode, count);
    }

    /**
     * 查询库存
     */
    public StorageEntity getStorage(String commodityCode) {
        return storageRepository.findByCommodityCode(commodityCode).orElse(null);
    }
}
