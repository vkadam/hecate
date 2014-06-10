package com.savoirtech.hecate.cql3.handler.pojo;

import com.savoirtech.hecate.cql3.convert.ValueConverter;
import com.savoirtech.hecate.cql3.handler.AbstractArrayHandler;
import com.savoirtech.hecate.cql3.meta.FacetMetadata;
import com.savoirtech.hecate.cql3.meta.PojoMetadata;
import com.savoirtech.hecate.cql3.persistence.QueryContext;
import com.savoirtech.hecate.cql3.persistence.SaveContext;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class PojoArrayHandler extends AbstractArrayHandler {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final FacetMetadata facetMetadata;
    private final PojoMetadata pojoMetadata;
    private final ValueConverter identifierConverter;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public PojoArrayHandler(FacetMetadata facetMetadata, PojoMetadata pojoMetadata, ValueConverter identifierConverter) {
        super(pojoMetadata.getPojoType(), identifierConverter.getDataType());
        this.facetMetadata = facetMetadata;
        this.pojoMetadata = pojoMetadata;
        this.identifierConverter = identifierConverter;
    }

//----------------------------------------------------------------------------------------------------------------------
// ColumnHandler Implementation
//----------------------------------------------------------------------------------------------------------------------


    @Override
    public Object getWhereClauseValue(Object parameterValue) {
        return identifierConverter.toCassandraValue(parameterValue);
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onFacetValueComplete(Object facetValue, QueryContext context) {
        final int length = Array.getLength(facetValue);
        final Map<Object, Object> pojos = new HashMap<>();
        for (int i = 0; i < length; ++i) {
            Object pojo = Array.get(facetValue, i);
            pojos.put(pojoMetadata.getIdentifierFacet().getFacet().get(pojo), pojo);
        }
        context.addPojos(pojoMetadata.getPojoType(), facetMetadata.getTableName(), pojos);
    }


    @Override
    protected Object toCassandraElement(Object facetElement, SaveContext context) {
        final Object identifierValue = pojoMetadata.getIdentifierFacet().getFacet().get(facetElement);
        Object pojo = pojoMetadata.newPojo(identifierValue);
        context.enqueue(pojoMetadata.getPojoType(), facetMetadata.getTableName(), pojo);
        return identifierConverter.toCassandraValue(identifierValue);
    }

    @Override
    protected Object toFacetElement(Object cassandraElement, QueryContext context) {
        return pojoMetadata.newPojo(identifierConverter.fromCassandraValue(cassandraElement));
    }
}