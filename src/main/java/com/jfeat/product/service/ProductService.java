/*
 *   Copyright (C) 2014-2016 www.kequandian.net
 *
 *    The program may be used and/or copied only with the written permission
 *    from www.kequandian.net or in accordance with the terms and
 *    conditions stipulated in the agreement/contract under which the program
 *    has been supplied.
 *
 *    All rights reserved.
 *
 */

package com.jfeat.product.service;

import com.google.common.collect.Lists;
import com.jfeat.config.model.Config;
import com.jfeat.core.BaseService;
import com.jfeat.core.UploadedFile;
import com.jfeat.product.exception.StockBalanceException;
import com.jfeat.product.model.*;
import com.jfinal.aop.Before;
import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by jacky on 3/9/16.
 */
public class ProductService extends BaseService {

    private static String MALL_AUTO_OFFSELL = "mall.auto_offsell";

    private static Logger logger = LoggerFactory.getLogger(ProductService.class);

    private String productUploadDir = "p";

    public String getProductUploadDir() {
        return productUploadDir;
    }

    public void setProductUploadDir(String productUploadDir) {
        this.productUploadDir = productUploadDir;
    }

    public List<ProductCategory> getProductCategories() {
        return ProductCategory.dao.findAllRecursively();
    }

    public Page<Product> paginateProducts(int pageNumber, int pageSize, String productName, String status) {
        return paginateProducts(pageNumber, pageSize, productName, status, null, null, null, null, null, null);
    }

    public Page<Product> paginateProducts(int pageNumber,
                                          int pageSize,
                                          String name,
                                          String status,
                                          Integer categoryId,
                                          Integer promoted,
                                          Integer zone,
                                          Integer purchaseStrategyId,
                                          String barCode,
                                          String storeLocation) {
        return Product.dao.paginate(pageNumber, pageSize, name, status, categoryId, promoted, zone, purchaseStrategyId, barCode, storeLocation);
    }

    public Ret deleteProduct(Integer id) {
        Product product = Product.dao.findById(id);
        if (product == null) {
            return failure("invalid.product");
        }
        Product.Status status = Product.Status.valueOf(product.getStatus());
        if (status == Product.Status.ONSELL) {
            return failure("product.delete.fail");
        }
        for (ProductImage productImage : product.getImages()) {
            UploadedFile.remove(productImage.getUrl());
        }
        for (ProductImage productImage : product.getCovers()) {
            UploadedFile.remove(productImage.getUrl());
        }
        product.delete();
        return success("product.delete.success");
    }

    @Before(Tx.class)
    public Ret createProduct(Product product, List<String> covers, String description, List<ProductSpecification> specifications) {
        product.setStatus(Product.Status.DRAFT.toString());
        product.save();

        for (int i = 0; i < covers.size(); i++) {
            String url = covers.get(i);
            product.addCover(url, i + 1);
            if (i == 0) {
                product.setCover(url);
                product.update();
            }
        }

        product.updateDescription(description);

        if (specifications == null) {
            specifications = new ArrayList<>();
        }
        for (ProductSpecification specification : specifications) {
            specification.setProductId(product.getId());
            if (specification.getCostPrice() == null) {
                specification.setCostPrice(product.getCostPrice());
            }
            if (specification.getSuggestedPrice() == null) {
                specification.setSuggestedPrice(product.getSuggestedPrice());
            }
            if (specification.getPrice() == null) {
                specification.setPrice(product.getPrice());
            }
        }
        Db.batchSave(specifications, 20);

        return success("product.create.success");
    }

    @Before(Tx.class)
    public Ret updateProduct(Product product,
                             List<String> newCovers,
                             Map<Integer, String> updatedCovers,
                             Integer[] unchangedCoverIds,
                             String description,
                             List<ProductSpecification> specifications) {

        boolean coverUpdated = false;
        Product originalProduct = Product.dao.findById(product.getId());
        List<ProductImage> originalCovers = ProductImage.dao.findByProductIdAndType(product.getId(), ProductImage.TYPE_COVER);
        List<ProductImage> toRemovedCovers = new LinkedList<>();
        for (ProductImage productImage : originalCovers) {
            boolean found = false;
            if (unchangedCoverIds != null) {
                for (Integer coverId : unchangedCoverIds) {
                    if (productImage.getId().equals(coverId)) {
                        found = true;
                        break;
                    }
                }
            }
            if (updatedCovers != null) {
                for (Integer coverId : updatedCovers.keySet()) {
                    if (productImage.getId().equals(coverId)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                toRemovedCovers.add(productImage);
            }

            for (Integer coverId : updatedCovers.keySet()) {
                if (productImage.getId().equals(coverId)) {
                    UploadedFile.remove(productImage.getUrl());
                    String url = updatedCovers.get(coverId);
                    productImage.setUrl(url);
                    productImage.update();
                    logger.debug("cover updated: {}", productImage);
                    break;
                }
            }
        }
        logger.debug("toRemovedCovers: " + toRemovedCovers);
        for (ProductImage productImage : toRemovedCovers) {
            productImage.delete();
        }

        Integer sortOrder = ProductImage.dao.queryMaxSortOrder(product.getId(), ProductImage.TYPE_COVER);
        for (int i = 0; i < newCovers.size(); i++) {
            String url = newCovers.get(i);
            product.addCover(url, ++sortOrder);
        }

        ProductImage firstCover = ProductImage.dao.findFirst(product.getId(), ProductImage.TYPE_COVER);
        if (firstCover != null && StrKit.notBlank(firstCover.getUrl()) && !firstCover.getUrl().equals(originalProduct.getCover())) {
            product.setCover(firstCover.getUrl());
            coverUpdated = true;
        }

        if (description != null) {
            product.updateDescription(description);
        }

        product.update();

        // updating specification
        List<ProductSpecification> originalSpecifications = product.getProductSpecifications();
        List<ProductSpecification> toAddSpecifications = Lists.newArrayList();
        List<ProductSpecification> toUpdateSpecifications = Lists.newArrayList();
        List<ProductSpecification> toRemoveSpecifications = Lists.newArrayList();
        List<ProductSpecification> toNotifyPriceUpdatedSpecifications = Lists.newArrayList();

        if (specifications == null) {
            specifications = Lists.newArrayList();
        }
        for (ProductSpecification specification : specifications) {
            if (specification.getId() == null) {
                specification.setProductId(product.getId());
                toAddSpecifications.add(specification);
            } else {
                for (ProductSpecification originalSpecification : originalSpecifications) {
                    if (specification.getId().equals(originalSpecification.getId())) {
                        toUpdateSpecifications.add(specification);
                    }
                    if (specification.getId().equals(originalSpecification.getId())
                            && specification.getPrice().compareTo(originalSpecification.getPrice()) != 0) {
                        toNotifyPriceUpdatedSpecifications.add(specification);
                    }
                }
            }
        }

        for (ProductSpecification originalSpecification : originalSpecifications) {
            boolean found = false;
            for (ProductSpecification specification : specifications) {
                if (specification.getId() != null && specification.getId().equals(originalSpecification.getId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                toRemoveSpecifications.add(originalSpecification);
            }
        }

        Db.batchSave(toAddSpecifications, 20);
        Db.batchUpdate(toUpdateSpecifications, 20);
        for (ProductSpecification toRemoveSpecification : toRemoveSpecifications) {
            toRemoveSpecification.delete();
        }

        if (toUpdateSpecifications.size() == 0 && originalProduct.getPrice().compareTo(product.getPrice()) != 0) {
            product.priceUpdatedNotify(null);
        }
        if (toNotifyPriceUpdatedSpecifications.size() > 0) {
            product.priceUpdatedNotify(toNotifyPriceUpdatedSpecifications);
        }

        if (coverUpdated) {
            product.coverUpdatedNotify();
        }

        return success("product.update.success");
    }

    public Ret deleteProductCategory(Integer id) {
        ProductCategory productCategory = ProductCategory.dao.findById(id);
        if (productCategory == null) {
            return failure("product_category.invalid.product_category");
        }
        if (productCategory.hasChildren() || productCategory.hasProduct()) {
            return failure("product_category.delete.has.children");
        }

        UploadedFile.remove(productCategory.getCover());
        productCategory.delete();
        return success("product_category.delete.success");
    }

    public void increaseProductViewCount(Integer productId) {
        Product.increaseViewCount(productId);
    }

    @Before(Tx.class)
    public void increaseProductSales(List<Integer> productIds, List<Integer> specificationIds, List<Integer> quantities) throws StockBalanceException {
        Product.increaseSales(productIds, specificationIds, quantities);
        offSellProduct(productIds);
    }

    @Before(Tx.class)
    public void decreaseProductSales(List<Integer> productIds, List<Integer> specificationIds, List<Integer> quantities) throws StockBalanceException {
        Product.decreaseSales(productIds, specificationIds, quantities);
    }

    /**
     * 更新搜索关键字
     *
     * @param name
     */
    public void updateHitWord(String name) {
        try {
            ProductHitWord hitWord = ProductHitWord.dao.findByName(name);
            if (hitWord != null) {
                hitWord.setHit(hitWord.getHit() + 1);
                hitWord.update();
            } else {
                hitWord = new ProductHitWord();
                hitWord.setName(name);
                hitWord.save();
            }
        } catch (Exception ex) {
            logger.error("update hit word error. {}", ex.getMessage());
        }
    }

    public void offSellProduct(List<Integer> productIds) {
        if (productIds == null || productIds.size() == 0) {
            return;
        }
        // 若未开启产品库存检查功能
        Config config = Config.dao.findByKey(MALL_AUTO_OFFSELL);
        if (config == null || config.getValueToBoolean() == null || !config.getValueToBoolean()) {
            logger.debug("库存检查功能状态：未开启");
            return;
        }
        //找出库存为<=0的产品，得到这些产品的id集合
        List<Integer> zeroStockBalanceProductIdList = new ArrayList<>();
        for (Integer productId : productIds) {
            Product product = Product.dao.findById(productId);
            if (product != null && product.getStockBalance() <= 0) {
                zeroStockBalanceProductIdList.add(productId);
            }
        }
        //把库存<=0的产品的状态设置为OFFSELL
        Product.dao.offSellProduct(zeroStockBalanceProductIdList);
    }
}
