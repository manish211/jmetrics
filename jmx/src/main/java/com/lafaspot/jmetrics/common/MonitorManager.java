/*
 * Copyright [yyyy] [name of copyright owner]
 * 
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  ====================================================================
 */

package com.lafaspot.jmetrics.common;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;

/**
 * Creates and hands out BaseMonitor objects to callers. Also has utility methods for any tasks that deal with the entire BaseMonitor collection.
 * @param <T> the type parameter
 * @author manish211
 */
public class MonitorManager<T extends BaseMonitor> {
    /**
     * The id used to distinguish monitors of different classloaders.
     * MonitorManager stores a unique id of the parent class loader.
     * This is to distinguish MonitorManagers in war style applications
     * in case the MonitorManager is loaded outside of war scope.
     */
    private String id;
    private final MonitorDirectory<T> monitorDirectory;
    private final Class<T> clazz;
    private final Set<String> constNamespaceSet;
    /**
     * Class Name of the Monitor class.
     */
    private final String className;
    /**
     * Canonical Name of the Monitor class.
     */
    private final String classCanonicalName;

    /**
     * Constructor.
     * @param clazz class
     * @param monitorDirectory MonitorDirectory Object
     * @param constNamespaceSet Set of namespace
     */
    public MonitorManager(final Class<T> clazz, final MonitorDirectory<T> monitorDirectory,
            final Set<String> constNamespaceSet) {
        this.clazz = clazz;
        this.monitorDirectory = monitorDirectory;
        this.constNamespaceSet = constNamespaceSet;
        if (id == null) {
            id = getMonitorManagerId(clazz);
        }
        this.className = clazz.getClass().getName();
        this.classCanonicalName = clazz.getCanonicalName();
    }
    /**
     * Provides a unique id for MonitorManagers defined in class loader for web apps or jvm scope.
     * @param clazz the class for which the monitor manager id is required
     * @return the monitor manager id of the class
     */
    public static String getMonitorManagerId(@Nonnull final Class<?> clazz) {
        return Integer.toString(clazz.getClassLoader().hashCode());
    }

    /**
     * Get the ID used to distinguish monitors of different classloaders.
     *
     * @return ID of classloader
     */
    public String getId() {
        return id;
    }

    /**
     * Manages handing out and creating Monitor objects. If monitor object does not exist it creates a new one. By default it adds
     * hostname|coloname|clustername|clusterType to namespace list while creating monitor.
     *
     * @param namespace List of names to be used in case monitor object needs to be created.
     * @return Monitor instance of type T corresponding to namespace defined in passed param.
     */
    public T getMonitor(@Nonnull final List<String> namespace) {
    	final Set<String> orderedNamespace = new TreeSet<>();
        orderedNamespace.addAll(namespace);
        orderedNamespace.addAll(constNamespaceSet);
        final StringBuilder stringBuilder = new StringBuilder();
        return monitorDirectory.getMonitor(stringBuilder.append(className).append(":namespace=")
        .append(String.join("|", orderedNamespace)).append(",type=").append(classCanonicalName).append(",id=")
        .append(id).toString());
    }

    /**
     * Returns host monitor for the give uri.
     *
     * @param uri Request uri
     * @return BaseMonitor instance
     */
    @Deprecated
    public T getHostMonitor(final URI uri) {
        final String beanName = uri.getScheme() + "-" + uri.getHost() + "-" + uri.getPort();
        return getHostStats(beanName);
    }

    /**
     * Manages handing out and creating Monitor objects.
     *
     * @param beanName The bean name to use in case the Monitor object needs to be created
     * @return A Monitor object for the given host
     */
    @Deprecated
    private T getHostStats(final String beanName) {
        return monitorDirectory.getMonitor(makeJmxName("hostname", beanName));
    }

    /**
     * Creates a JMX name given the logical name.
     *
     * @param name The logical name of the bean
     *
     * @return A JMX-friendly name for the bean
     */
    @Deprecated
    private String makeJmxName(final String category, final String name) {
        final StringBuilder jmxNameBuilder = new StringBuilder();
        jmxNameBuilder.append(className).append(":category=").append(category).append(",type=").append(classCanonicalName)
                .append(",name=").append(name);
        return jmxNameBuilder.toString();
    }
}