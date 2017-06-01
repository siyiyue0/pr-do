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
import com.jfeat.product.model.base.ProductFavoriteBase;
import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.List;

@TableBind(tableName = "t_product_favorite")
public class ProductFavorite extends ProductFavoriteBase<ProductFavorite> {

    /**
     * Only use for query.
     */
    public static ProductFavorite dao = new ProductFavorite();

    public Page<Record> productPaginate(int pageNumber, int pageSize, int userId) {
        String select = "select p.*";
        List<Object> params = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        builder.append("from ");
        builder.append(getTableName());
        builder.append(" as pf ");
        builder.append(" join ");
        builder.append(Product.dao.getTableName());
        builder.append(" as p ");
        builder.append(" on pf.product_id=p.id ");
        builder.append(" where pf.user_id=? ");
        params.add(userId);

        return Db.paginate(pageNumber, pageSize, select, builder.toString(), params.toArray());
    }

    public ProductFavorite find(int userId, int productId) {
        String sql = new SqlQuery().from(getTableName()).where(Fields.USER_ID.eq("?")).and(Fields.PRODUCT_ID.eq("?")).sql();
        return findFirst(sql, userId, productId);
    }
}