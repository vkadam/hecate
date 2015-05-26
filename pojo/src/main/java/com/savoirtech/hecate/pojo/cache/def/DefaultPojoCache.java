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

package com.savoirtech.hecate.pojo.cache.def;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.savoirtech.hecate.pojo.cache.PojoCache;
import com.savoirtech.hecate.pojo.mapping.PojoMapping;
import com.savoirtech.hecate.pojo.persistence.PersistenceContext;
import com.savoirtech.hecate.pojo.util.PojoMetricsUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultPojoCache implements PojoCache {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    public static final int DEFAULT_MAX_SIZE = 5000;

    private final PersistenceContext persistenceContext;

    private final LoadingCache<PojoMapping<? extends Object>,LoadingCache<Object,Object>> caches = CacheBuilder.newBuilder().build(new OuterCacheLoader());

    private final int defaultMaxCacheSize;
    private final Map<PojoMapping<? extends Object>,Integer> maxCacheSizes;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public DefaultPojoCache(PersistenceContext persistenceContext) {
        this(persistenceContext, Collections.emptyMap(), DEFAULT_MAX_SIZE);
    }

    public DefaultPojoCache(PersistenceContext persistenceContext, Map<PojoMapping<? extends Object>,Integer> maxCacheSizes) {
        this(persistenceContext, maxCacheSizes, DEFAULT_MAX_SIZE);
    }

    public DefaultPojoCache(PersistenceContext persistenceContext, int defaultMaxCacheSize) {
        this(persistenceContext, new HashMap<>(), defaultMaxCacheSize);
    }

    public DefaultPojoCache(PersistenceContext persistenceContext,Map<PojoMapping<? extends Object>,Integer> maxCacheSizes, int defaultMaxCacheSize) {
        this.persistenceContext = persistenceContext;
        this.maxCacheSizes = maxCacheSizes;
        this.defaultMaxCacheSize= defaultMaxCacheSize;
    }

//----------------------------------------------------------------------------------------------------------------------
// PojoCache Implementation
//----------------------------------------------------------------------------------------------------------------------

    @Override
    public boolean contains(PojoMapping<? extends Object> mapping) {
        return size(mapping) > 0;
    }

    @Override
    public Set<Object> idSet(PojoMapping<? extends Object> mapping) {
        LoadingCache<Object, Object> cache = cacheForMapping(mapping);
        return cache == null ? Collections.emptySet() : cache.asMap().keySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P> P lookup(PojoMapping<P> mapping, Object id) {
        return (P)caches.getUnchecked(mapping).getUnchecked(id);
    }

    @Override
    public <P> void put(PojoMapping<P> mapping, Object id, P pojo) {
        caches.getUnchecked(mapping).put(id,pojo);
    }

    @Override
    public <P> void putAll(PojoMapping<P> mapping, Iterable<P> pojos) {
        LoadingCache<Object, Object> cache = caches.getUnchecked(mapping);
        for (P pojo : pojos) {
            cache.put(mapping.getForeignKeyMapping().getColumnValue(pojo), pojo);
        }
    }

    @Override
    public long size(PojoMapping<? extends Object> mapping) {
        LoadingCache<Object, Object> cache = cacheForMapping(mapping);
        return cache == null ? 0 : cache.size();
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    protected <P> LoadingCache<Object, Object> cacheForMapping(PojoMapping<P> mapping) {
        return caches.asMap().get(mapping);
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private class InnerCacheLoader extends CacheLoader<Object, Object> {
        private final PojoMapping<? extends Object> mapping;

        public InnerCacheLoader(PojoMapping<? extends Object> mapping) {
            this.mapping = mapping;
        }

        @Override
        public Object load(Object id) throws Exception {
            PojoMetricsUtils.createCounter(mapping, "cacheMiss").inc();
            return persistenceContext.findById(mapping).execute(id).one();
        }
    }

    private class OuterCacheLoader extends CacheLoader<PojoMapping<? extends Object>, LoadingCache<Object, Object>> {
        @Override
        public LoadingCache<Object, Object> load(final PojoMapping<? extends Object> mapping) throws Exception {
            return CacheBuilder.newBuilder().maximumSize(maxCacheSizes.getOrDefault(mapping,defaultMaxCacheSize)).build(new InnerCacheLoader(mapping));
        }
    }
}