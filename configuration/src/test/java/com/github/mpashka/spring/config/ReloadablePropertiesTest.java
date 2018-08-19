package com.github.mpashka.spring.config;

import com.github.mpashka.spring.config.beans.PropsBean;
import com.github.mpashka.spring.config.beans.PropsBeanMix;
import com.github.mpashka.spring.config.beans.PropsBeanParentXml;
import com.github.mpashka.spring.config.utils.ContentServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

/**
 * PropsTest
 *
 * @author Pavel Moukhataev
 */
public class ReloadablePropertiesTest {

    private static final Logger log = LoggerFactory.getLogger(ReloadablePropertiesTest.class);

    private static final boolean TEST_ANNOTATIONS = true;
    private static final boolean TEST_XML = true;

    private static ContentServer propsServer;

    @BeforeClass
    public static void init() throws Exception {
        propsServer = new ContentServer("PropertiesServer", 8080);
    }

    @AfterClass
    public static void after() {
        propsServer.stop();
    }

    @Test
    public void testResourceReload() {
        testResourceReload(ctx -> {
            EnvironmentConfigurationSource configurationSource = ctx.getBean(EnvironmentConfigurationSource.class);
            configurationSource.updateSources();
/*
            ReloadableConfigurationSupport reloadableConfigurationSupport = ctx.getBean(ReloadableConfigurationSupport.class);
            reloadableConfigurationSupport.onApplicationEvent(new ConfigurationUpdatedEvent(this, emptyList()));
*/
        });
    }


    @Test
    public void testResourceReloadByTimeout() {
        testResourceReload(ctx ->
        {
            log.trace("Wait for update");
//            ApplicationListener listener = ctx.getBean(ApplicationListener.class);
//            listener.sub
            try {
                // This time must be higher then org.github.spring.config.EnvironmentConfigurationSource.periodMs
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error("Unexpected interruption", e);
            }
        });
    }

    private void testResourceReload(Consumer<ApplicationContext> propertyReloader) {
        propsServer.setContent("/test1.properties",
                 "p1=1.0\n"
                +"p3=3.0\n"
                +"p4=4\n"
                +"s5=Hello World\n"
                +"p6ref=ref1\n"
                +"p7=p7.1\n"
                +"p8i=1\n"
                +"p9=p9.1\n"
                +"valueProperty=vp1\n"
                +"valueSetter=vs1\n"
                +"noGetter=ng1\n"
        );

        propsServer.setContent("/test2.properties",
                "p2=2.0\n"
                        + "pd1=1.0\n"
                        + "pd2=2\n"
        );

        // open/read the application context file
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"PropsTest-context.xml"});

        if (TEST_ANNOTATIONS) {
            PropsBean bean = ctx.getBean(PropsBean.class);
            assertThat(bean.getP1(), is("1.0"));
            assertThat(bean.getP2(), is("2.0"));
            assertThat(bean.getPd1(), closeTo(1.0, 0.0));
            assertThat(bean.getPd2(), is(2));
            assertThat(bean.getS5(), is("Hello World"));
            assertThat(bean.getP6ref(), is("Ref1"));
            assertThat(bean.getP8spel(), is(3));
            PropsBeanMix beanMix = ctx.getBean(PropsBeanMix.class);
            assertThat(beanMix.getP1(), is("1.0"));
            assertThat(beanMix.getP2(), is("2.0"));
            assertThat(beanMix.getPd1(), closeTo(1.0, 0.0));
            assertThat(beanMix.getPd2(), is(2));
            assertThat(beanMix.getS5b(), is("Hello World"));
            assertThat(beanMix.getP6refb(), is("Ref1"));
        }

        if (TEST_XML) {
            PropsBeanParentXml beanXml = ctx.getBean(PropsBeanParentXml.class);
            assertThat(beanXml.getP3(), is("3.0"));
            assertThat(beanXml.getP3Count(), is(1));
            assertThat(beanXml.getP4(), is(4));
            assertThat(beanXml.getP4Count(), is(1));
            assertThat(beanXml.getS5(), is("Hello World"));
            assertThat(beanXml.getS5Count(), is(1));
            assertThat(beanXml.getP6ref(), is("Ref1"));
            assertThat(beanXml.getP6refCount(), is(1));
            assertThat(beanXml.getChildXml().getP7(), is("p7.1"));
            assertThat(beanXml.getChildXmlList(), notNullValue());
            assertThat(beanXml.getChildXmlList().size(), is(1));
            assertThat(beanXml.getChildXmlList().get(0).getP9(), is("p9.1"));
            assertThat(beanXml.nogetterCheckValueProperty(), is("vp1"));
            assertThat(beanXml.getCheckValueSetter(), is("vs1"));
            assertThat(beanXml.nogetterCheckNoGetter(), is("ng1"));
            PropsBeanMix beanMix = ctx.getBean(PropsBeanMix.class);
            assertThat(beanMix.getP3(), is("3.0"));
            assertThat(beanMix.getP3Count(), is(1));
            assertThat(beanMix.getP4(), is(4));
            assertThat(beanMix.getP4Count(), is(1));
            assertThat(beanMix.getS5x(), is("Hello World"));
            assertThat(beanMix.getS5xCount(), is(1));
            assertThat(beanMix.getP6refx(), is("Ref1"));
            assertThat(beanMix.getP6refxCount(), is(1));
        }

        propsServer.setContent("/test1.properties",
                "p1=1.1\n"
                        +"p3=3.1\n"
                        +"p4=4\n"
                        +"s5=Bye World\n"
                        +"p6ref=ref2\n"
                        +"p7=p7.2\n"
                        +"p8i=2\n"
                        +"p9=p9.2\n"
                        +"valueProperty=vp2\n"
                        +"valueSetter=vs2\n"
                        +"noGetter=ng2\n"
        );

        propertyReloader.accept(ctx);

        if (TEST_ANNOTATIONS) {
            PropsBean bean = ctx.getBean(PropsBean.class);
            assertThat(bean.getP1(), is("1.1"));
            assertThat(bean.getP2(), is("2.0"));   // Not reloadable
            assertThat(bean.getPd1(), closeTo(1.0, 0.0)); // in configuration 2
            assertThat(bean.getPd2(), is(2));  // Not reloadable
            assertThat(bean.getS5(), is("Bye World"));
            assertThat(bean.getP6ref(), is("Ref2"));
            assertThat(bean.getP8spel(), is(2));
            PropsBeanMix beanMix = ctx.getBean(PropsBeanMix.class);
            assertThat(beanMix.getP1(), is("1.1"));
            assertThat(beanMix.getP2(), is("2.0"));   // Not reloadable
            assertThat(beanMix.getPd1(), closeTo(1.0, 0.0)); // in configuration 2
            assertThat(beanMix.getPd2(), is(2));  // Not reloadable
            assertThat(beanMix.getS5b(), is("Bye World"));
            assertThat(beanMix.getP6refb(), is("Ref2"));
        }

        if (TEST_XML) {
            PropsBeanParentXml beanXml = ctx.getBean(PropsBeanParentXml.class);
            assertThat(beanXml.getP3(), is("3.1"));
            assertThat(beanXml.getP3Count(), is(2));
            assertThat(beanXml.getP4(), is(4));
            assertThat(beanXml.getP4Count(), is(1));
            assertThat(beanXml.getS5(), is("Bye World"));
            assertThat(beanXml.getS5Count(), is(2));
            assertThat(beanXml.getP6ref(), is("Ref2"));
            assertThat(beanXml.getP6refCount(), is(2));
            assertThat(beanXml.getChildXml().getP7(), is("p7.2"));
            assertThat(beanXml.getChildXmlList(), notNullValue());
            assertThat(beanXml.getChildXmlList().size(), is(1));
            assertThat(beanXml.getChildXmlList().get(0).getP9(), is("p9.2"));
            assertThat(beanXml.nogetterCheckValueProperty(), is("vp2"));
            assertThat(beanXml.getCheckValueSetter(), is("vs2"));
            assertThat(beanXml.nogetterCheckNoGetter(), is("ng2"));
            PropsBeanMix beanMix = ctx.getBean(PropsBeanMix.class);
            assertThat(beanMix.getP3(), is("3.1"));
            assertThat(beanMix.getP3Count(), is(2));
            assertThat(beanMix.getP4(), is(4));
            assertThat(beanMix.getP4Count(), is(1));
            assertThat(beanMix.getS5x(), is("Bye World"));
            assertThat(beanMix.getS5xCount(), is(2));
            assertThat(beanMix.getP6refx(), is("Ref2"));
            assertThat(beanMix.getP6refxCount(), is(2));
        }

        propsServer.setContent("/test2.properties",
                "p2=2.1\n"
                        +"pd1=1.1\n"
                        +"pd2=3\n"
        );

        propertyReloader.accept(ctx);

        if (TEST_ANNOTATIONS) {
            PropsBean bean = ctx.getBean(PropsBean.class);
            assertThat(bean.getP1(), is("1.1"));
            assertThat(bean.getP2(), is("2.1"));
            assertThat(bean.getPd1(), closeTo(1.1, 0.0));
            assertThat(bean.getPd2(), is(3));
            assertThat(bean.getS5(), is("Bye World"));
            assertThat(bean.getP6ref(), is("Ref2"));
            PropsBeanMix beanMix = ctx.getBean(PropsBeanMix.class);
            assertThat(beanMix.getP1(), is("1.1"));
            assertThat(beanMix.getP2(), is("2.1"));
            assertThat(beanMix.getPd1(), closeTo(1.1, 0.0));
            assertThat(beanMix.getPd2(), is(3));
            assertThat(beanMix.getS5b(), is("Bye World"));
            assertThat(beanMix.getP6refb(), is("Ref2"));
        }

        if (TEST_XML) {
            PropsBeanParentXml beanXml = ctx.getBean(PropsBeanParentXml.class);
            assertThat(beanXml.getP3(), is("3.1"));
            assertThat(beanXml.getP3Count(), is(2));
            assertThat(beanXml.getP4(), is(4));
            assertThat(beanXml.getP4Count(), is(1));
            assertThat(beanXml.getS5(), is("Bye World"));
            assertThat(beanXml.getS5Count(), is(2));
            assertThat(beanXml.getP6ref(), is("Ref2"));
            assertThat(beanXml.getP6refCount(), is(2));
            assertThat(beanXml.getChildXml().getP7(), is("p7.2"));
            assertThat(beanXml.nogetterCheckValueProperty(), is("vp2"));
            assertThat(beanXml.getCheckValueSetter(), is("vs2"));
            assertThat(beanXml.nogetterCheckNoGetter(), is("ng2"));
            PropsBeanMix beanMix = ctx.getBean(PropsBeanMix.class);
            assertThat(beanMix.getP3(), is("3.1"));
            assertThat(beanMix.getP3Count(), is(2));
            assertThat(beanMix.getP4(), is(4));
            assertThat(beanMix.getP4Count(), is(1));
            assertThat(beanMix.getS5x(), is("Bye World"));
            assertThat(beanMix.getS5xCount(), is(2));
            assertThat(beanMix.getP6refx(), is("Ref2"));
            assertThat(beanMix.getP6refxCount(), is(2));
        }
    }

}
