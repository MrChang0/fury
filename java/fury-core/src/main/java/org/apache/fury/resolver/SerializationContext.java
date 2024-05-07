/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fury.resolver;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.fury.config.FuryBuilder;
import org.apache.fury.util.Preconditions;

/**
 * A context is used to add some context-related information, so that the serializers can set up
 * relation between serializing different objects. The context will be reset after finished
 * serializing/deserializing the object tree.
 */
public final class SerializationContext {
  private final IdentityHashMap<Object, Object> objects = new IdentityHashMap<>();
  private MetaContext metaContext;
  private final Map<String, UserContext> userContextResolvers = new LinkedHashMap<>();

  /** Return the previous value associated with <tt>key</tt>, or <tt>null</tt>. */
  public Object add(Object key, Object value) {
    return objects.put(key, value);
  }

  public boolean containsKey(Object key) {
    return objects.containsKey(key);
  }

  public Object get(Object key) {
    return objects.get(key);
  }

  public MetaContext getMetaContext() {
    return metaContext;
  }

  /**
   * Set meta context, which can be used to share data across multiple serialization call. Note that
   * {@code metaContext} will be cleared after the serialization is finished. Please set the context
   * before every serialization if metaShare is enabled by {@link
   * FuryBuilder#withMetaContextShare(boolean)}
   */
  public void setMetaContext(MetaContext metaContext) {
    this.metaContext = metaContext;
  }

  public Map<String, UserContext> getUserContextResolvers() {
    return userContextResolvers;
  }

  public void registerUserContextResolver(String name, UserContext userContext) {
    Preconditions.checkState(!userContextResolvers.containsKey(name));
    userContextResolvers.put(name, userContext);
  }

  public void reset() {
    if (objects.size() > 0) {
      objects.clear();
    }
    metaContext = null;
    for (UserContext userContext : userContextResolvers.values()) {
      userContext.reset();
    }
  }
}
