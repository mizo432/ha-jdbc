/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2012  Paul Ferraro
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
package net.sf.hajdbc.balancer;

import java.io.Serializable;
import java.util.Set;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.Identifiable;

/**
 * バランサーを作成するためのファクトリです。
 * @author Paul Ferraro
 */
public interface BalancerFactory extends Identifiable, Serializable
{
	/**
	 * バランサーを作成します。
	 * @param <Z> データベース接続ソース
	 * @param <D> データベース記述子
	 * @param databases 初期データベースのセット
	 * @return 新しいバランサー
	 */
	<Z, D extends Database<Z>> Balancer<Z, D> createBalancer(Set<D> databases);
}
