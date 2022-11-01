package com.kinetica.gserv.test;

import java.time.Instant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.kinetica.gvis.resource.ResourcesBase;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TestSmeService {
    
    private static final Logger LOG = LoggerFactory.getLogger(TestSmeService.class);
    
    @Autowired
    private MockMvc mvc;
    
    @Test
    public void testGetStatus() throws Exception {
        this.mvc.perform(
                MockMvcRequestBuilders.get("/status"))
                .andExpect(MockMvcResultMatchers.content()
                        .contentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("OK"));
    }
    
    @Test
    public void testGetMsi() throws Exception {
        String projectId = "SME_Yellowstone";
        Instant transStart = ResourcesBase.parseKineticaDate("2022-05-18 14:00:00");
        Instant transEnd = ResourcesBase.parseKineticaDate("2022-05-18 17:23:43");
        
        
        this.mvc.perform(
                MockMvcRequestBuilders.get("/getSphereEvents")
                    .param("projectId", projectId)
                    .param("startTs", transStart.toString())
                    .param("endTs", transEnd.toString())
                )
                //.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM));
    }
    
    @Test
    public void testGetSphereJson() throws Exception {
        String projectId = "SME_Yellowstone";
        Instant transStart = ResourcesBase.parseKineticaDate("2022-05-18 14:00:00");
        Instant transEnd = ResourcesBase.parseKineticaDate("2022-05-18 17:23:43");
        
        
        this.mvc.perform(
                MockMvcRequestBuilders.get("/getSphereEventsJson")
                    .param("projectId", projectId)
                    .param("startTs", transStart.toString())
                    .param("endTs", transEnd.toString())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_JSON));
        
        //String content = result.getResponse().getContentAsString();
        //LOG.info("response: {}", content);
    }
    

    @Test
    public void testGetTop() throws Exception {
        String projectId = "SME_GlacierBay";
        int layer = 0;
        double lat1 = 27.9736278;
        double lon1 = -99.7692978;
        double lat2 = 28.0010957;
        double lon2 = -99.7437286;
        String color = "FFFF00";
        
        MvcResult result = this.mvc.perform(
                MockMvcRequestBuilders.get("/getTop")
                    .param("projectId", projectId)
                    .param("layer", Integer.toString(layer))
                    .param("lat1", Double.toString(lat1))
                    .param("lon1", Double.toString(lon1))
                    .param("lat2", Double.toString(lat2))
                    .param("lon2", Double.toString(lon2))
                    .param("color", color)
                )
                //.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andReturn();
        
        MockHttpServletResponse response = result.getResponse();
        LOG.info("getContentLength: {}", response.getContentLength());
    }
    
}
