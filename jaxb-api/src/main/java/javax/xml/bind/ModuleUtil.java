/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package javax.xml.bind;

import java.io.IOException;

/**
 * Intended to be overridden on JDK9, with JEP 238 multi-release class copy.
 * Contains only stubs for methods needed on JDK9 runtime.
 *
 * @author Roman Grigoriadi
 */
class ModuleUtil {

    /**
     * Resolves classes from context path.
     * Only one class per package is needed to access its {@link java.lang.Module}
     */
    static Class[] getClassesFromContextPath(String contextPath, ClassLoader classLoader) throws JAXBException {
        return null;
    }

    /**
     * Find first class in package by {@code jaxb.index} file.
     */
    static Class findFirstByJaxbIndex(String pkg, ClassLoader classLoader) throws IOException, JAXBException {
        return null;
    }

    /**
     * Implementation may be defined in other module than {@code java.xml.bind}. In that case openness
     * {@linkplain java.lang.Module#isOpen open} of classes should be delegated to implementation module.
     *
     * @param classes used to resolve module for {@linkplain java.lang.Module#addOpens(String, java.lang.Module)}
     * @param factorySPI used to resolve {@link java.lang.Module} of the implementation.
     */
    static void delegateAddOpensToImplModule(Class[] classes, Class<?> factorySPI) {
        //stub
    }

}
