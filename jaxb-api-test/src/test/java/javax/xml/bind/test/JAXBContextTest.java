/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2018 Oracle and/or its affiliates. All rights reserved.
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

package javax.xml.bind.test;


import jaxb.test.usr.A;
import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.xml.bind.JAXBContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import static junit.framework.TestCase.assertTrue;

/*
 * test for JDK-8131334: SAAJ Plugability Layer: using java.util.ServiceLoader
 *
 * There are unsafe scenarios not to be run within the build (modifying jdk files).
 * To run those, following needs to be done:
 *   1. allow java to write into $JAVA_HOME/conf: mkdir $JAVA_HOME/conf; chmod a+rw $JAVA_HOME/conf
 *   2. use "runUnsafe" property: mvn clean test -DrunUnsafe=true
 */
@RunWith(Parameterized.class)
public class JAXBContextTest {

    static final Logger logger = Logger.getLogger(JAXBContextTest.class.getName());

    static final Boolean skipUnsafe = !Boolean.getBoolean("runUnsafe");

    // test configuration ------------------------------------------

    // test-classes directory (required for setup and for security settings)
    static final String classesDir = JAXBContextTest.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    private static final String FACTORY_ID_LEGACY = "javax.xml.bind.context.factory";
    private static final String FACTORY_ID = "javax.xml.bind.JAXBContextFactory";
    private static final String PACKAGE_LEGACY = "jaxb.factory.legacy."; // TODO: ???
    private static final String PACKAGE_SPI = "jaxb.factory.spi."; // TODO: ???
    private static final Object DEFAULT = "com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl";


    static {
        System.setProperty("classesDir", classesDir);
    }

    // configuration to be created by the test
    static Path providersDir = Paths.get(classesDir, "META-INF", "services");
    static Path providersFileLegacy = providersDir.resolve("javax.xml.bind.JAXBContext");
    static Path providersFile = providersDir.resolve("javax.xml.bind.JAXBContextFactory");

    // configuration to be created by the test
    static Path jaxbPropsDir = Paths.get(classesDir, "jaxb", "test", "usr");
    static Path jaxbPropsFile = jaxbPropsDir.resolve("jaxb.properties");

    // test instance -----------------------------------------------

    // scenario name - just for logging
    String scenario;

    // java policy file for testing w/security manager
    private String expectedFactory;
    private Class<?> expectedException;


    @Parameterized.Parameters
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                // scenario-name, jaxb.properties, svc, arg1, arg2, system-props
                {"scenario-1", FACTORY_ID_LEGACY + "="+PACKAGE_LEGACY+"Valid", null, PACKAGE_LEGACY+"Valid$JAXBContext1", null, null},
                {"scenario-3", FACTORY_ID_LEGACY + "=non.existing.FactoryClass", null, null, javax.xml.bind.JAXBException.class, null},
                {"scenario-4", FACTORY_ID_LEGACY + "="+PACKAGE_LEGACY+"Invalid", null, null, javax.xml.bind.JAXBException.class, null},
                {"scenario-13", FACTORY_ID_LEGACY + "="+PACKAGE_LEGACY+"Valid", PACKAGE_LEGACY+"Valid2", PACKAGE_LEGACY+"Valid$JAXBContext1", null, PACKAGE_LEGACY+"Valid3"},

                {"scenario-1", FACTORY_ID_LEGACY + "="+PACKAGE_SPI+"Valid", null, PACKAGE_SPI+"Valid$JAXBContext1", null, null},
                {"scenario-3", FACTORY_ID_LEGACY + "=non.existing.FactoryClass", null, null, javax.xml.bind.JAXBException.class, null},
                {"scenario-4", FACTORY_ID_LEGACY + "="+PACKAGE_SPI+"Invalid", null, null, javax.xml.bind.JAXBException.class, null},
                {"scenario-13", FACTORY_ID_LEGACY + "="+PACKAGE_SPI+"Valid", PACKAGE_SPI+"Valid2", PACKAGE_SPI+"Valid$JAXBContext1", null, PACKAGE_SPI+"Valid3"},

                {"scenario-1", FACTORY_ID + "="+PACKAGE_SPI+"Valid", null, PACKAGE_SPI+"Valid$JAXBContext1", null, null},
                {"scenario-3", FACTORY_ID + "=non.existing.FactoryClass", null, null, javax.xml.bind.JAXBException.class, null},
                {"scenario-4", FACTORY_ID + "="+PACKAGE_SPI+"Invalid", null, null, javax.xml.bind.JAXBException.class, null},
                {"scenario-13", FACTORY_ID + "="+PACKAGE_SPI+"Valid", PACKAGE_SPI+"Valid2", PACKAGE_SPI+"Valid$JAXBContext1", null, PACKAGE_SPI+"Valid3"},

                {"scenario-1", FACTORY_ID + "="+PACKAGE_LEGACY+"Valid", null, PACKAGE_LEGACY+"Valid$JAXBContext1", null, null},
                {"scenario-3", FACTORY_ID + "=non.existing.FactoryClass", null, null, javax.xml.bind.JAXBException.class, null},
                {"scenario-4", FACTORY_ID + "="+PACKAGE_LEGACY+"Invalid", null, null, javax.xml.bind.JAXBException.class, null},
                {"scenario-13", FACTORY_ID + "="+PACKAGE_LEGACY+"Valid", PACKAGE_LEGACY+"Valid2", PACKAGE_LEGACY+"Valid$JAXBContext1", null, PACKAGE_LEGACY+"Valid3"},


                {"scenario-2", "something=AnotherThing", null, null, javax.xml.bind.JAXBException.class, null},

                // service loader
                {"scenario-8", null, PACKAGE_SPI+"Valid\n", PACKAGE_SPI+"Valid$JAXBContext1", null, null},
                {"scenario-9", null, PACKAGE_SPI+"Valid", PACKAGE_SPI+"Valid$JAXBContext1", null, null},
                {"scenario-11", null, PACKAGE_SPI+"Invalid", null, javax.xml.bind.JAXBException.class, null},
                {"scenario-15", null, PACKAGE_SPI+"Valid", PACKAGE_SPI+"Valid$JAXBContext1", null, null},

                // service loader - legacy
                {"scenario-8 legacy-svc", null, PACKAGE_SPI+"Valid\n", PACKAGE_SPI+"Valid$JAXBContext1", null, null},
                {"scenario-9 legacy-svc", null, PACKAGE_SPI+"Valid", PACKAGE_SPI+"Valid$JAXBContext1", null, null},
                {"scenario-11 legacy-svc", null, PACKAGE_SPI+"Invalid", null, javax.xml.bind.JAXBException.class, null},
                {"scenario-15 legacy-svc", null, PACKAGE_SPI+"Valid", PACKAGE_SPI+"Valid$JAXBContext1", null, null},

                // service loader - legacy
                {"scenario-8 legacy-svc", null, PACKAGE_LEGACY+"Valid\n", PACKAGE_LEGACY+"Valid$JAXBContext1", null, null},
                {"scenario-9 legacy-svc", null, PACKAGE_LEGACY+"Valid", PACKAGE_LEGACY+"Valid$JAXBContext1", null, null},
                {"scenario-11 legacy-svc", null, PACKAGE_LEGACY+"Invalid", null, javax.xml.bind.JAXBException.class, null},
                {"scenario-15 legacy-svc", null, PACKAGE_LEGACY+"Valid", PACKAGE_LEGACY+"Valid$JAXBContext1", null, null},

                // system property
                {"scenario-5", null, null, PACKAGE_SPI+"Valid$JAXBContext1", null, PACKAGE_SPI+"Valid"},
                {"scenario-7", null, null, null, javax.xml.bind.JAXBException.class, PACKAGE_SPI+"Invalid"},
                {"scenario-14", null, PACKAGE_SPI+"Valid2", PACKAGE_SPI+"Valid$JAXBContext1", null, PACKAGE_SPI+"Valid"},

                {"scenario-5", null, null, PACKAGE_LEGACY+"Valid$JAXBContext1", null, PACKAGE_LEGACY+"Valid"},
                {"scenario-7", null, null, null, javax.xml.bind.JAXBException.class, PACKAGE_LEGACY+"Invalid"},
                {"scenario-14", null, PACKAGE_LEGACY+"Valid2", PACKAGE_LEGACY+"Valid$JAXBContext1", null, PACKAGE_LEGACY+"Valid"},
                {"scenario-6", null, null, null, javax.xml.bind.JAXBException.class, "jaxb.factory.NonExisting"},

                {"scenario-10", null, "jaxb.factory.NonExisting", null, javax.xml.bind.JAXBException.class, null},

                {"scenario-12", null, null, DEFAULT, javax.xml.bind.JAXBException.class, null},
        });
    }

    // scenario-name, jaxb.properties, svc, arg1, arg2, system-props
    public JAXBContextTest(
            String scenario,
            String jaxbPropertiesClass,
            String spiClass,
            String expectedFactory,
            Class<?> expectedException,
            String systemProperty
    ) {

        // ensure setup may be done ...
        System.setSecurityManager(null);

        if (systemProperty != null) {
            System.setProperty("javax.xml.bind.JAXBContextFactory", systemProperty);
        } else {
            System.clearProperty("javax.xml.bind.JAXBContextFactory");
        }

        this.scenario = scenario;
        this.expectedFactory = expectedFactory;
        this.expectedException = expectedException;

        if (skipUnsafe && scenario.startsWith("unsafe")) {
            log("Skipping unsafe scenario:" + scenario);
            return;
        }

        prepare(jaxbPropertiesClass, spiClass);
    }

    @Test
    public void testPath() throws IOException {
        logConfigurations();
        try {
            JAXBContext ctx = JAXBContext.newInstance("jaxb.test.usr");
            handleResult(ctx);
        } catch (Throwable throwable) {
            handleThrowable(throwable);
        } finally {
            doFinally();
        }
    }

    @Test
    public void testClasses() throws IOException {
        logConfigurations();
        try {
            JAXBContext ctx = JAXBContext.newInstance(new Class[] {A.class}, null);
            handleResult(ctx);
        } catch (Throwable throwable) {
            handleThrowable(throwable);
        } finally {
            doFinally();
        }
    }

    @Test
    public void testClass() throws IOException {
        logConfigurations();
        try {
            JAXBContext ctx = JAXBContext.newInstance(A.class);
            handleResult(ctx);
        } catch (Throwable throwable) {
            handleThrowable(throwable);
        } finally {
            doFinally();
        }
    }

    private void handleResult(JAXBContext ctx) {
        assertTrue("No ctx found.", ctx != null);
        log("     TEST: context class = [" + ctx.getClass().getName() + "]\n");
        String className = ctx.getClass().getName();
        assertTrue("Incorrect ctx: [" + className + "], Expected: [" + expectedFactory + "]",
                className.equals(expectedFactory));

        log(" TEST PASSED");
    }

    private void handleThrowable(Throwable throwable) {
        if (throwable instanceof AssertionFailedError) throw ((AssertionFailedError)throwable);
        Class<?> throwableClass = throwable.getClass();
        boolean correctException = throwableClass.equals(expectedException);
        if (!correctException) {
            throwable.printStackTrace();
        }
        if (expectedException == null) {
            throw new AssertionFailedError("Unexpected exception:" + throwableClass);
        }
        assertTrue("Got unexpected exception: [" + throwableClass + "], expected: [" + expectedException + "]",
                correctException);
        log(" TEST PASSED");
    }

    private void doFinally() {
        cleanResource(providersFile);
        //cleanResource(providersDir);

        // unsafe; not running:
        cleanResource(jaxbPropsFile);
        System.setSecurityManager(null);
    }

    @Test
    public void testPathSM() throws IOException {
        enableSM();
        testPath();
    }

    @Test
    public void testClassSM() throws IOException {
        enableSM();
        testClass();
    }

    @Test
    public void testClassesSM() throws IOException {
        enableSM();
        testClasses();
    }


    private void enableSM() {
        System.setSecurityManager(null);
        System.setProperty("java.security.policy", classesDir + "javax/xml/bind/test.policy");
        System.setSecurityManager(new SecurityManager());
    }

    private void cleanResource(Path resource) {
        try {
            if (Files.exists(resource)) {
                Files.deleteIfExists(resource);
            }
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }

    private void prepare(String propertiesClassName, String providerClassName) {

        try {
            log("providerClassName = " + providerClassName);
            log("propertiesClassName = " + propertiesClassName);

            cleanResource(providersFile);
            cleanResource(providersFileLegacy);
            if (scenario.contains("legacy-svc")) {
                setupFile(providersFileLegacy, providersDir, providerClassName);
            } else {
                setupFile(providersFile, providersDir, providerClassName);
            }


            // unsafe; not running:
            if (propertiesClassName != null) {
                setupFile(jaxbPropsFile, jaxbPropsDir, propertiesClassName);
            } else {
                cleanResource(jaxbPropsFile);
            }

            log(" SETUP OK.");

        } catch (IOException e) {
            log(" SETUP FAILED.");
            e.printStackTrace();
        }
    }

    private void logConfigurations() throws IOException {
        logFile(providersFile);
        logFile(providersFileLegacy);
        logFile(jaxbPropsFile);
    }

    private void logFile(Path path) throws IOException {
        if (Files.exists(path)) {
            log("File [" + path + "] exists: [");
            log(new String(Files.readAllBytes(path)));
            log("]");
        }
    }

    private void setupFile(Path file, Path dir, String value) throws IOException {
        cleanResource(file);
        if (value != null) {
            log("writing configuration [" + value + "] into file [" + file.toAbsolutePath() + "]");
            Files.createDirectories(dir);
            Files.write(
                    file,
                    value.getBytes(),
                    StandardOpenOption.CREATE);
        }
    }

    private void log(String msg) {
        logger.info("[" + scenario + "] " + msg);
//        System.out.println("[" + scenario + "] " + msg);
    }

}

