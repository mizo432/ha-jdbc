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
package net.sf.hajdbc.xml;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO model for HA-JDBC XML configuration.
 * @author Paul Ferraro
 */
@JacksonXmlRootElement(localName = "ha-jdbc")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "xmlns", "distributable", "sync", "state", "lock", "cluster"
})
public class Configuration
{
    @JacksonXmlProperty(isAttribute = true, localName = "xmlns")
    private String xmlns;

    @JacksonXmlProperty(localName = "distributable")
    private IdentifiableDescriptor distributable;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "sync")
    private List<IdentifiedDescriptor> sync = new ArrayList<>();

    @JacksonXmlProperty(localName = "state")
    private IdentifiableDescriptor state;

    @JacksonXmlProperty(localName = "lock")
    private IdentifiableDescriptor lock;

    @JacksonXmlProperty(localName = "cluster")
    private Cluster cluster;

    public IdentifiableDescriptor getDistributable() {
        return distributable;
    }

    public void setDistributable(IdentifiableDescriptor distributable) {
        this.distributable = distributable;
    }

    public List<IdentifiedDescriptor> getSync() {
        return sync;
    }

    public void setSync(List<IdentifiedDescriptor> sync) {
        this.sync = sync;
    }

    public IdentifiableDescriptor getState() {
        return state;
    }

    public void setState(IdentifiableDescriptor state) {
        this.state = state;
    }

    public IdentifiableDescriptor getLock() {
        return lock;
    }

    public void setLock(IdentifiableDescriptor lock) {
        this.lock = lock;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public String getNamespace() {
        return xmlns;
    }

    public void setNamespace(String xmlns) {
        this.xmlns = xmlns;
    }
}
