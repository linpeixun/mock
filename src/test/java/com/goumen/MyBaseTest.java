package com.goumen;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by zhaomeng on 16/5/5.
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF-TEST/config/spring-config.xml",
        "classpath:META-INF-TEST/config/spring-mvc.xml"})
@WebAppConfiguration
@DbUnitConfiguration(databaseConnection = {"dataSource"})
@TestExecutionListeners({
//        DependencyInjectionTestExecutionListener.class,
        MockitoDependencyInjectionTestExecutionListener.class
//        DirtiesContextTestExecutionListener.class,
//        TransactionDbUnitTestExecutionListener.class
})
public abstract class MyBaseTest {


    @Before
    public void setUp() throws Exception {
//        MockitoAnnotations.initMocks(this);

    }
}
