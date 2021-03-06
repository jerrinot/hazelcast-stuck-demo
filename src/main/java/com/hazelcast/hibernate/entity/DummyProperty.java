/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.hibernate.entity;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.IOException;

@Entity
@Table(name = "dummy_props")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DummyProperty implements DataSerializable {

    @Id
    @Column(name = "uid")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "version")
    private int version;

    @Column(name = "key")
    private String key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_uid")
    private DummyEntity entity;

    public DummyProperty() {
    }

    public DummyProperty(String key) {
        super();
        this.key = key;
    }

    public DummyProperty(String key, DummyEntity entity) {
        super();
        this.key = key;
        this.entity = entity;
    }

    public DummyProperty(long id, String key, DummyEntity entity) {
        super();
        this.id = id;
        this.key = key;
        this.entity = entity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public DummyEntity getEntity() {
        return entity;
    }

    public void setEntity(DummyEntity entity) {
        this.entity = entity;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(id);
        out.writeInt(version);
        out.writeUTF(key);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        id = in.readLong();
        version = in.readInt();
        key = in.readUTF();
    }
}
