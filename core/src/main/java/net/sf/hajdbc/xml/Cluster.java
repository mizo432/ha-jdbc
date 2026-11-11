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

import java.util.ArrayList;
import java.util.List;

/**
 * POJO model for HA-JDBC cluster configuration.
 * @author Paul Ferraro
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "default-sync", "balancer", "meta-data-cache", "dialect", "durability",
    "input-sink", "transaction-mode", "auto-activate-schedule", "failure-detect-schedule",
    "eval-current-date", "eval-current-time", "eval-current-timestamp", "eval-rand",
    "detect-identity-columns", "detect-sequences", "allow-empty-cluster", "database"
})
public class Cluster
{
    @JacksonXmlProperty(isAttribute = true, localName = "default-sync")
    private String defaultSync;

    @JacksonXmlProperty(isAttribute = true)
    private String balancer;

    @JacksonXmlProperty(isAttribute = true, localName = "meta-data-cache")
    private String metaDataCache;

    @JacksonXmlProperty(isAttribute = true)
    private String dialect;

    @JacksonXmlProperty(isAttribute = true)
    private String durability;

    @JacksonXmlProperty(isAttribute = true, localName = "input-sink")
    private String inputSink;

    @JacksonXmlProperty(isAttribute = true, localName = "transaction-mode")
    private String transactionMode;

    @JacksonXmlProperty(isAttribute = true, localName = "auto-activate-schedule")
    private String autoActivateSchedule;

    @JacksonXmlProperty(isAttribute = true, localName = "failure-detect-schedule")
    private String failureDetectSchedule;

    @JacksonXmlProperty(isAttribute = true, localName = "eval-current-date")
    private Boolean evalCurrentDate;

    @JacksonXmlProperty(isAttribute = true, localName = "eval-current-time")
    private Boolean evalCurrentTime;

    @JacksonXmlProperty(isAttribute = true, localName = "eval-current-timestamp")
    private Boolean evalCurrentTimestamp;

    @JacksonXmlProperty(isAttribute = true, localName = "eval-rand")
    private Boolean evalRand;

    @JacksonXmlProperty(isAttribute = true, localName = "detect-identity-columns")
    private Boolean detectIdentityColumns;

    @JacksonXmlProperty(isAttribute = true, localName = "detect-sequences")
    private Boolean detectSequences;

    @JacksonXmlProperty(isAttribute = true, localName = "allow-empty-cluster")
    private Boolean allowEmptyCluster;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "database")
    private List<XmlDatabase> database = new ArrayList<>();

    public String getDefaultSync() {
        return defaultSync;
    }

    public void setDefaultSync(String defaultSync) {
        this.defaultSync = defaultSync;
    }

    public String getBalancer() {
        return balancer;
    }

    public void setBalancer(String balancer) {
        this.balancer = balancer;
    }

    public String getMetaDataCache() {
        return metaDataCache;
    }

    public void setMetaDataCache(String metaDataCache) {
        this.metaDataCache = metaDataCache;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public String getDurability() {
        return durability;
    }

    public void setDurability(String durability) {
        this.durability = durability;
    }

    public String getInputSink() {
        return inputSink;
    }

    public void setInputSink(String inputSink) {
        this.inputSink = inputSink;
    }

    public String getTransactionMode() {
        return transactionMode;
    }

    public void setTransactionMode(String transactionMode) {
        this.transactionMode = transactionMode;
    }

    public String getAutoActivateSchedule() {
        return autoActivateSchedule;
    }

    public void setAutoActivateSchedule(String autoActivateSchedule) {
        this.autoActivateSchedule = autoActivateSchedule;
    }

    public String getFailureDetectSchedule() {
        return failureDetectSchedule;
    }

    public void setFailureDetectSchedule(String failureDetectSchedule) {
        this.failureDetectSchedule = failureDetectSchedule;
    }

    public Boolean getEvalCurrentDate() {
        return evalCurrentDate;
    }

    public void setEvalCurrentDate(Boolean evalCurrentDate) {
        this.evalCurrentDate = evalCurrentDate;
    }

    public Boolean getEvalCurrentTime() {
        return evalCurrentTime;
    }

    public void setEvalCurrentTime(Boolean evalCurrentTime) {
        this.evalCurrentTime = evalCurrentTime;
    }

    public Boolean getEvalCurrentTimestamp() {
        return evalCurrentTimestamp;
    }

    public void setEvalCurrentTimestamp(Boolean evalCurrentTimestamp) {
        this.evalCurrentTimestamp = evalCurrentTimestamp;
    }

    public Boolean getEvalRand() {
        return evalRand;
    }

    public void setEvalRand(Boolean evalRand) {
        this.evalRand = evalRand;
    }

    public Boolean getDetectIdentityColumns() {
        return detectIdentityColumns;
    }

    public void setDetectIdentityColumns(Boolean detectIdentityColumns) {
        this.detectIdentityColumns = detectIdentityColumns;
    }

    public Boolean getDetectSequences() {
        return detectSequences;
    }

    public void setDetectSequences(Boolean detectSequences) {
        this.detectSequences = detectSequences;
    }

    public Boolean getAllowEmptyCluster() {
        return allowEmptyCluster;
    }

    public void setAllowEmptyCluster(Boolean allowEmptyCluster) {
        this.allowEmptyCluster = allowEmptyCluster;
    }

    public List<XmlDatabase> getDatabase() {
        return database;
    }

    public void setDatabase(List<XmlDatabase> database) {
        this.database = database;
    }
}
