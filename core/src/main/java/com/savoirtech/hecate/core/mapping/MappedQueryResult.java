/*
 * Copyright (c) 2012-2015 Savoir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.savoirtech.hecate.core.mapping;

import com.datastax.driver.core.ResultSet;
import com.google.common.collect.Iterators;
import com.savoirtech.hecate.core.query.QueryResult;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class MappedQueryResult<T> implements QueryResult<T> {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final ResultSet resultSet;
    private final RowMapper<T> mapper;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public MappedQueryResult(ResultSet resultSet, RowMapper<T> mapper) {
        this.resultSet = resultSet;
        this.mapper = mapper;
    }

//----------------------------------------------------------------------------------------------------------------------
// QueryResult Implementation
//----------------------------------------------------------------------------------------------------------------------

    @Override
    public Iterator<T> iterator() {
        return Iterators.transform(resultSet.iterator(), mapper::map);
    }

    @Override
    public List<T> list() {
        return resultSet.all().stream().map(mapper::map).collect(Collectors.toList());
    }

    @Override
    public T one() {
        return mapper.map(resultSet.one());
    }
}