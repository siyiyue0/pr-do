/*
 *   Copyright (C) 2014-2017 www.kequandian.net
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
 * It defines fields related to the database table.
 *
 * DON'T EDIT IT. OTHERWIDE IT WILL BE OVERRIDE WHEN RE-GENERATING IF TABLE CHANGE.
 */
package com.jfeat.product.model.base;

import com.jfinal.plugin.activerecord.IBean;
import com.jfeat.core.BaseModel;
import java.math.BigDecimal;
import java.util.Date;

public abstract class ProductCategoryPropertyBase<M extends ProductCategoryPropertyBase<?>> extends BaseModel<M> implements IBean {

    /**
     * Table fields 
     */
    public enum Fields {
        ID("id"),
        CATEGORY_ID("category_id"),
        DISPLAY_NAME("display_name"),
        VALUE_TYPE("value_type"),
        INPUT_TYPE("input_type"),
        CANDIDATE_VALUES("candidate_values"),
        DEFAULT_VALUE("default_value"),
        IS_REQUIRED("is_required"),
        SORT_ORDER("sort_order");
        
        private String name;
        Fields(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
        public String like(Object obj) {
            return new StringBuilder(this.toString()).append(" LIKE ").append(obj).toString();
        }
        public String eq(Object obj) {
            return new StringBuilder(this.toString()).append("=").append(obj).toString();
        }
        public String ge(Object obj) {
            return new StringBuilder(this.toString()).append(">=").append(obj).toString();
        }
        public String lt(Object obj) {
            return new StringBuilder(this.toString()).append("<").append(obj).toString();
        }
        public String le(Object obj) {
            return new StringBuilder(this.toString()).append("<=").append(obj).toString();
        }
        public String isNull() {
            return new StringBuilder(this.toString()).append(" IS NULL").toString();
        }
        public String notNull() {
            return new StringBuilder(this.toString()).append(" NOT NULL").toString();
        }
        public String notEquals(Object obj) {
            return new StringBuilder(this.toString()).append("<>").append(obj).toString();
        }
    }

    public void setId(Integer var) {
        set(Fields.ID.toString(), var);
    }

    public Integer getId() {
        return (Integer) get(Fields.ID.toString());
    }

    public void setCategoryId(Integer var) {
        set(Fields.CATEGORY_ID.toString(), var);
    }

    public Integer getCategoryId() {
        return (Integer) get(Fields.CATEGORY_ID.toString());
    }

    public void setDisplayName(String var) {
        set(Fields.DISPLAY_NAME.toString(), var);
    }

    public String getDisplayName() {
        return (String) get(Fields.DISPLAY_NAME.toString());
    }

    public void setValueType(String var) {
        set(Fields.VALUE_TYPE.toString(), var);
    }

    public String getValueType() {
        return (String) get(Fields.VALUE_TYPE.toString());
    }

    public void setInputType(String var) {
        set(Fields.INPUT_TYPE.toString(), var);
    }

    public String getInputType() {
        return (String) get(Fields.INPUT_TYPE.toString());
    }

    public void setCandidateValues(String var) {
        set(Fields.CANDIDATE_VALUES.toString(), var);
    }

    public String getCandidateValues() {
        return (String) get(Fields.CANDIDATE_VALUES.toString());
    }

    public void setDefaultValue(String var) {
        set(Fields.DEFAULT_VALUE.toString(), var);
    }

    public String getDefaultValue() {
        return (String) get(Fields.DEFAULT_VALUE.toString());
    }

    public void setIsRequired(Integer var) {
        set(Fields.IS_REQUIRED.toString(), var);
    }

    public Integer getIsRequired() {
        return (Integer) get(Fields.IS_REQUIRED.toString());
    }

    public void setSortOrder(Integer var) {
        set(Fields.SORT_ORDER.toString(), var);
    }

    public Integer getSortOrder() {
        return (Integer) get(Fields.SORT_ORDER.toString());
    }


    ///////////////////////////////////////////////////////////////////////////
    
}
