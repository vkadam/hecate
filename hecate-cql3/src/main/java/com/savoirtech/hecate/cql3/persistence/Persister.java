package com.savoirtech.hecate.cql3.persistence;

public interface Persister {
//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    PojoFindByKey findByKey();

    PojoSave save();

    PojoFindByKeys findByKeys();
}
