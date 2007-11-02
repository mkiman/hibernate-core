/*
 * Copyright (c) 2007, Red Hat Middleware, LLC. All rights reserved.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, v. 2.1. This program is distributed in the
 * hope that it will be useful, but WITHOUT A WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. You should have received a
 * copy of the GNU Lesser General Public License, v.2.1 along with this
 * distribution; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Red Hat Author(s): Steve Ebersole
 */
package org.hibernate.cache.jbc2;

import java.util.Properties;

import org.hibernate.cache.CacheDataDescription;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CollectionRegion;
import org.hibernate.cache.EntityRegion;
import org.hibernate.cache.QueryResultsRegion;
import org.hibernate.cache.RegionFactory;
import org.hibernate.cache.TimestampsRegion;
import org.hibernate.cache.jbc2.builder.JndiSharedCacheInstanceManager;
import org.hibernate.cache.jbc2.builder.SharedCacheInstanceManager;
import org.hibernate.cache.jbc2.collection.CollectionRegionImpl;
import org.hibernate.cache.jbc2.entity.EntityRegionImpl;
import org.hibernate.cache.jbc2.query.QueryResultsRegionImpl;
import org.hibernate.cache.jbc2.timestamp.TimestampsRegionImpl;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.Settings;
import org.hibernate.util.PropertiesHelper;
import org.jboss.cache.DefaultCacheFactory;

;

/**
 * {@link RegionFactory} that uses one or more JBoss Cache instances for 
 * caching entities, collections, queries and timestamps. How the factory
 * obtains a reference to the needed JBoss Cache instance(s) is determined
 * by the injected {@link CacheInstanceManager}.
 * <p>
 * By default uses {@link SharedCacheInstanceManager} as its
 * {@link #getCacheInstanceManager() CacheInstanceManager}.
 * Basically, this uses a single shared JBoss Cache for entities, collections,
 * queries and timestamps. The JBoss Cache instance is created by the
 * JBC {@link DefaultCacheFactory} using the resource identified by the
 * {@link JndiSharedCacheInstanceManager#CACHE_RESOURCE_PROP}
 * configuration property. 
 * </p>
 * <p>
 * Also exposes an overloaded constructor that allows injection of different
 * <code>CacheInstanceManager</code> implementations.
 * </p>
 * 
 * @author Steve Ebersole
 * @author Brian Stansberry
 */
public class JBossCacheRegionFactory implements RegionFactory {
    private CacheInstanceManager cacheInstanceManager;

    /**
     * FIXME Per the RegionFactory class Javadoc, this constructor version
     * should not be necessary.
     * 
     * @param props
     */
    public JBossCacheRegionFactory(Properties props) {
        this();
    }

    /**
     *  Create a new JBossCacheRegionFactory.
     */
    public JBossCacheRegionFactory() {
    }

    /**
     * Create a new JBossCacheRegionFactory that uses the provided
     * {@link CacheInstanceManager}.
     * 
     * @param cacheInstanceManager
     */
    public JBossCacheRegionFactory(CacheInstanceManager cacheInstanceManager) {
        this.cacheInstanceManager = cacheInstanceManager;
    }

    public CacheInstanceManager getCacheInstanceManager() {
        return cacheInstanceManager;
    }

    public void start(Settings settings, Properties properties) throws CacheException {
        if (cacheInstanceManager == null) {
            cacheInstanceManager = new SharedCacheInstanceManager();
        }

        cacheInstanceManager.start(settings, properties);
    }

    public void stop() {
        if (cacheInstanceManager != null) {
            cacheInstanceManager.stop();
        }
    }

    public boolean isMinimalPutsEnabledByDefault() {
        return true;
    }

    public long nextTimestamp() {
        return System.currentTimeMillis() / 100;
    }

    public EntityRegion buildEntityRegion(String regionName, Properties properties, CacheDataDescription metadata)
            throws CacheException {
        return new EntityRegionImpl(cacheInstanceManager.getEntityCacheInstance(), regionName,
                getRegionPrefix(properties), metadata);
    }

    public CollectionRegion buildCollectionRegion(String regionName, Properties properties,
            CacheDataDescription metadata) throws CacheException {
        return new CollectionRegionImpl(cacheInstanceManager.getCollectionCacheInstance(), regionName,
                getRegionPrefix(properties), metadata);
    }

    public QueryResultsRegion buildQueryResultsRegion(String regionName, Properties properties) throws CacheException {

        return new QueryResultsRegionImpl(cacheInstanceManager.getQueryCacheInstance(), regionName,
                getRegionPrefix(properties), properties);
    }

    public TimestampsRegion buildTimestampsRegion(String regionName, Properties properties) throws CacheException {

        return new TimestampsRegionImpl(cacheInstanceManager.getTimestampsCacheInstance(), regionName,
                getRegionPrefix(properties), properties);
    }

    public static String getRegionPrefix(Properties properties) {
        return PropertiesHelper.getString(Environment.CACHE_REGION_PREFIX, properties, null);
    }

}