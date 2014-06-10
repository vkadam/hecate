package com.savoirtech.hecate.cql3.handler.pojo;

import com.datastax.driver.core.DataType;
import com.savoirtech.hecate.cql3.convert.ValueConverter;
import com.savoirtech.hecate.cql3.handler.ColumnHandler;
import com.savoirtech.hecate.cql3.meta.FacetMetadata;
import com.savoirtech.hecate.cql3.meta.PojoMetadata;
import com.savoirtech.hecate.cql3.persistence.QueryContext;
import com.savoirtech.hecate.cql3.persistence.SaveContext;

public class PojoValueHandler implements ColumnHandler {
    private final FacetMetadata facetMetadata;
    private final PojoMetadata pojoMetadata;
    private final ValueConverter identifierConverter;
    private final DataType columnType;

    public PojoValueHandler(FacetMetadata facetMetadata, PojoMetadata pojoMetadata, ValueConverter identifierConverter) {
        this.facetMetadata = facetMetadata;
        this.pojoMetadata = pojoMetadata;
        this.identifierConverter = identifierConverter;
        this.columnType = identifierConverter.getDataType();
    }

    @Override
    public DataType getColumnType() {
        return columnType;
    }

    @Override
    public Object getInsertValue(Object facetValue, SaveContext context) {
        if (facetValue == null) {
            return null;
        }
        Object identifierValue = pojoMetadata.getIdentifierFacet().getFacet().get(facetValue);
        context.enqueue(pojoMetadata.getPojoType(), facetMetadata.getTableName(), facetValue);
        return identifierConverter.toCassandraValue(identifierValue);
    }

    @Override
    public Object getFacetValue(Object cassandraValue, QueryContext context) {
        if (cassandraValue == null) {
            return null;
        }
        Object pojo = pojoMetadata.newPojo(identifierConverter.fromCassandraValue(cassandraValue));
        context.addPojo(pojoMetadata.getPojoType(), facetMetadata.getTableName(), cassandraValue, pojo);
        return pojo;
    }

    @Override
    public Object getWhereClauseValue(Object parameterValue) {
        if (parameterValue == null) {
            return null;
        }
        return pojoMetadata.getIdentifierFacet().getFacet().get(parameterValue);
    }
}
