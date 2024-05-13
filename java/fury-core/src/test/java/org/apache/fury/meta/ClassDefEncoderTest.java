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

package org.apache.fury.meta;

import static org.apache.fury.meta.ClassDefEncoder.buildFieldsInfo;
import static org.apache.fury.meta.ClassDefEncoder.getClassFields;

import java.util.HashMap;
import java.util.List;
import lombok.Data;
import org.apache.fury.Fury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.resolver.MetaContext;
import org.apache.fury.test.bean.BeanA;
import org.apache.fury.test.bean.MapFields;
import org.apache.fury.test.bean.Struct;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ClassDefEncoderTest {

  @Test
  public void testBasicClassDef() throws Exception {
    Fury fury = Fury.builder().withMetaContextShare(true).build();
    Class<ClassDefTest.TestFieldsOrderClass1> type = ClassDefTest.TestFieldsOrderClass1.class;
    List<ClassDef.FieldInfo> fieldsInfo = buildFieldsInfo(fury.getClassResolver(), type);
    MemoryBuffer buffer =
        ClassDefEncoder.encodeClassDef(
            fury.getClassResolver(), type, getClassFields(type, fieldsInfo), new HashMap<>());
    ClassDef classDef = ClassDef.readClassDef(fury.getClassResolver(), buffer);
    Assert.assertEquals(classDef.getClassName(), type.getName());
    Assert.assertEquals(classDef.getFieldsInfo().size(), type.getDeclaredFields().length);
    Assert.assertEquals(classDef.getFieldsInfo(), fieldsInfo);
  }

  @Test
  public void testBigMetaEncoding() {
    for (Class<?> type :
        new Class[] {
          MapFields.class, BeanA.class, Struct.createStructClass("TestBigMetaEncoding", 5)
        }) {
      Fury fury = Fury.builder().withMetaContextShare(true).build();
      ClassDef classDef = ClassDef.buildClassDef(fury, type);
      ClassDef classDef1 =
          ClassDef.readClassDef(
              fury.getClassResolver(), MemoryBuffer.fromByteArray(classDef.getEncoded()));
      Assert.assertEquals(classDef1, classDef);
    }
  }

  @Data
  public static class Foo1 {
    private int f1;
  }

  @Data
  public static class Foo2 extends Foo1 {}

  @Test
  public void testEmptySubClassSerializer() {
    final Foo2 foo2 = new Foo2();
    foo2.setF1(100);

    Fury fury =
        Fury.builder()
            .withLanguage(Language.JAVA)
            .requireClassRegistration(false)
            .withCompatibleMode(CompatibleMode.COMPATIBLE)
            .withMetaContextShare(true)
            .build();
    fury.getSerializationContext().setMetaContext(new MetaContext());
    final byte[] serialize = fury.serialize(foo2);
    fury.getSerializationContext().setMetaContext(new MetaContext());

    Assert.assertEquals(foo2, fury.deserialize(serialize));
  }
}
