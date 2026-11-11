/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2014  Paul Ferraro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.hajdbc.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Tests for ObjectInputStream class
 */
public class ObjectInputStreamTest {

    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }

    @Test
    public void deserializeString() throws IOException {
        byte[] bytes = serialize("Hello, World!");
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes), this.getClass().getClassLoader())) {
            Object result = ois.readObject();
            assertThat(result).isEqualTo("Hello, World!");
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    @Test
    public void deserializeInteger() throws IOException {
        byte[] bytes = serialize(42);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes), this.getClass().getClassLoader())) {
            Object result = ois.readObject();
            assertThat(result).isEqualTo(42);
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    @Test
    public void deserializeArrayList() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        byte[] bytes = serialize(list);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes), this.getClass().getClassLoader())) {
            @SuppressWarnings("unchecked")
            List<String> result = (List<String>) ois.readObject();
            assertThat(result).containsExactly("a", "b", "c");
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    @Test
    public void deserializeHashMap() throws IOException {
        Map<String, Integer> map = new HashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        byte[] bytes = serialize(map);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes), this.getClass().getClassLoader())) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> result = (Map<String, Integer>) ois.readObject();
            assertThat(result).containsEntry("one", 1).containsEntry("two", 2);
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    @Test
    public void deserializeBoolean() throws IOException {
        byte[] bytes = serialize(true);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes), this.getClass().getClassLoader())) {
            Object result = ois.readObject();
            assertThat((Boolean) result).isTrue();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    @Test
    public void deserializeByteArray() throws IOException {
        byte[] original = {1, 2, 3, 4, 5};
        byte[] bytes = serialize(original);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes), this.getClass().getClassLoader())) {
            Object result = ois.readObject();
            assertThat(result).isEqualTo(original);
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    @Test
    public void deserializeWithCustomClassLoader() throws IOException {
        byte[] bytes = serialize("test");
        ClassLoader customLoader = this.getClass().getClassLoader();
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes), customLoader)) {
            Object result = ois.readObject();
            assertThat(result).isEqualTo("test");
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    @Test
    public void deserializeSerializableObject() throws IOException {
        TestSerializable obj = new TestSerializable();
        obj.name = "test";
        obj.value = 123;
        byte[] bytes = serialize(obj);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes), this.getClass().getClassLoader())) {
            Object result = ois.readObject();
            assertThat(result).isInstanceOf(TestSerializable.class);
            assertThat(((TestSerializable) result).name).isEqualTo("test");
            assertThat(((TestSerializable) result).value).isEqualTo(123);
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    @Test
    public void deserializeEnum() throws IOException {
        byte[] bytes = serialize(TestEnum.FOO);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes), this.getClass().getClassLoader())) {
            Object result = ois.readObject();
            assertThat(result).isEqualTo(TestEnum.FOO);
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    static class TestSerializable implements Serializable {
        private static final long serialVersionUID = 1L;
        String name;
        int value;
    }

    enum TestEnum {
        FOO, BAR, BAZ
    }
}
