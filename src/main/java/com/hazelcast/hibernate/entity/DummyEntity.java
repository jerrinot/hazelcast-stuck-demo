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
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "dummy_entities")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DummyEntity implements DataSerializable {

    @Id
    @Column(name = "uid")
    private long id;

    @Column(name = "version")
    private int version;

    @Column(name = "name")
    private String name;

    @Column(name = "dummy_value")
    private double value;

    @Column(name = "dummy_date")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "properties")
    @JoinColumn(name = "parent_uid")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<DummyProperty> properties;

    public DummyEntity() {
        super();
    }

    public DummyEntity(long id, String name, double value, Date date) {
        super();
        this.id = id;
        this.name = name;
        this.value = value;
        this.date = date;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setProperties(List<DummyProperty> properties) {
        this.properties = properties;
    }

    public List<DummyProperty> getProperties() {
        return properties;
    }

    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(id);
        out.writeUTF(name);
        out.writeDouble(value);
        out.writeObject(date);
    }

    public void readData(ObjectDataInput in) throws IOException {
        id = in.readLong();
        name = in.readUTF();
        value = in.readDouble();
        date = in.readObject();
    }
}
