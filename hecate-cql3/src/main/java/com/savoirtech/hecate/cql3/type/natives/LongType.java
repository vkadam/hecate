package com.savoirtech.hecate.cql3.type.natives;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;

public class LongType extends NativeType<Long> {
//----------------------------------------------------------------------------------------------------------------------
// ColumnType Implementation
//----------------------------------------------------------------------------------------------------------------------


    @Override
    public Long extractValue(Row row, int columnIndex) {
        return row.getLong(columnIndex);
    }

    @Override
    public DataType getDataType() {
        return DataType.bigint();
    }
}
