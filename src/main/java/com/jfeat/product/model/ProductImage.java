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

/*
 * This file is automatically generated by tools.
 * It defines the model for the table. All customize operation should 
 * be written here. Such as query/update/delete.
 * The controller calls this object.
 */
package com.jfeat.product.model;

import com.jfeat.kit.SqlQuery;
import com.jfeat.product.model.base.ProductImageBase;
import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Db;

import java.util.List;

@TableBind(tableName = "t_product_image")
public class ProductImage extends ProductImageBase<ProductImage> {

    public static final int TYPE_COVER = 0;
    public static final int TYPE_DETAIL = 1;

    /**
     * Only use for query.
     */
    public static ProductImage dao = new ProductImage();


    public List<ProductImage> findByProductIdAndType(int productId, int type) {
        SqlQuery query = new SqlQuery();
        query.from(getTableName());
        query.where(Fields.PRODUCT_ID.eq("?"));
        query.and(Fields.TYPE.eq("?"));
        query.orderBy(Fields.SORT_ORDER.toString());
        return find(query.sql(), productId, type);
    }

    public List<ProductImage> findByProductId(int productId) {
        SqlQuery query = new SqlQuery();
        query.from(getTableName());
        query.where(Fields.PRODUCT_ID.eq("?"));
        return find(query.sql(), productId);
    }

    public Integer queryMaxSortOrder(int productId, int type) {
        Integer sortOrder = Db.queryInt("select max(sort_order) from t_product_image where product_id=? and type=?", productId, type);
        return sortOrder == null ? 0 : sortOrder;
    }

    public ProductImage findFirst(int productId, int type) {
        return findFirst("select * from t_product_image where product_id=? and type=? order by sort_order", productId, type);
    }
}