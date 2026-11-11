package net.sf.hajdbc.management;

import net.sf.hajdbc.Database;

/**
 * Factory for creating {@link MBeanRegistrar} instances.
 * <p>
 * This factory is a core component of the HA-JDBC architecture, registered via
 * {@link net.sf.hajdbc.DatabaseClusterConfiguration#getMBeanRegistrarFactory()}.
 * The MBean registrar exposes JMX management beans for monitoring and managing
 * the database cluster, including activation status, balancer state, and performance metrics.
 * </p>
 *
 * @see MBeanRegistrar
 * @see net.sf.hajdbc.DatabaseClusterConfiguration#getMBeanRegistrarFactory()
 */
public interface MBeanRegistrarFactory
{
	/**
	 * Creates a new MBean registrar.
	 * @param <Z> database connection source
	 * @param <D> database descriptor
	 * @return new MBean registrar
	 */
	<Z, D extends Database<Z>> MBeanRegistrar<Z, D> createMBeanRegistrar();
}
