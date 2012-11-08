package org.jboss.pressgang.belay.oauth2.authserver.rest.test;

import org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint.TestJaxRsActivator;
import org.jboss.pressgang.belay.oauth2.authserver.util.Resources;
import org.jboss.pressgang.belay.util.test.unit.BaseUnitTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;

import javax.persistence.EntityManager;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public class BaseArquillianEndpointImplTest extends BaseUnitTest {

    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "arquilliantest.war")
                .addPackage(TestJaxRsActivator.class.getPackage())
                .addPackages(true, "org.jboss.pressgang.belay.oauth2.authserver")
                .addClass(Resources.class)
                .addClass(EntityManager.class)
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsResource("test-resourceserver.properties", "META-INF/resourceserver.properties")
                .addAsResource("test-authserver.properties", "META-INF/authserver.properties")
                .addAsResource("test-import.sql", "import.sql")
                .addAsWebInfResource("jbossas-ds.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("test-web.xml", "web.xml")
                .addAsLibraries(DependencyResolvers.use(MavenDependencyResolver.class)
                        .includeDependenciesFromPom("src/test/resources/general/test-pom.xml")
                        .resolveAs(JavaArchive.class));
    }

    public static String getBaseTestUrl() {
        return "https://localhost:8443/arquilliantest/rest";
    }
}
