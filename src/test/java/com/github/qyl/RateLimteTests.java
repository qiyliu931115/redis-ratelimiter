package com.github.qyl;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RateLimteTests {

    @Autowired
    private WebApplicationContext context ;
    private MockMvc mvc ;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mvc = MockMvcBuilders.webAppContextSetup(context).build();  //构造MockMvc
    }
    @Test
    public void testGet5() throws Exception{
        log.info("@test: 测试令牌桶开始");
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/test/get5")).andExpect(status().isOk()).andReturn();
        String string = mvcResult.getResponse().getContentAsString();
        Assert.assertFalse(string.contains("2xxSuccessful"));
        log.info("@test: 测试令牌桶结束");
    }


}
