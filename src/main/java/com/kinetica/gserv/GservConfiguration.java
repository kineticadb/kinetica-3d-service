/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 */

package com.kinetica.gserv;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import com.gpudb.GPUdb;
import com.gpudb.GPUdbBase;
import com.gpudb.GPUdbException;
import com.gpudb.protocol.ShowSystemPropertiesRequest;
import com.gpudb.protocol.ShowSystemPropertiesResponse;
import com.kinetica.gvis.GvisConfigTerrain;

/**
 * Configuration object that collects parameters from the application.properties.
 * @author Chad Juliano
 */
@Component
@Validated
@ConfigurationProperties(prefix="gserv")
public class GservConfiguration {
    
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String url;
    
    private final GvisConfigTerrain _config = new GvisConfigTerrain();
    
    public void setGpudbUsername(String username) {
        this.username = username;
    }
    
    public void setGpudbPassword(String password) {
        this.password = password;
    }
    
    public void setGpudbUrl(String url) {
        this.url = url;
    }
    
    public void setShowTerrainLabel(boolean _val) {
        this._config._showTerrainLabel = _val;
    }
    
    public void setDisableCache(boolean _val) {
        this._config.setAllCacheEnabled(false);
        this._config._tileCacheEnabled = !_val;
    }
    
    public void setSchema(String _val) {
    	this._config._schema = _val;
    }
    
    public GvisConfigTerrain getConfig() throws Exception {
        this._config._dbc = getConnection();
        return this._config;
    }

    private GPUdb getConnection() throws Exception {
        GPUdbBase.Options _options = new GPUdbBase.Options();
        //option.setTimeout(0);
        _options.setUsername(this.username);
        _options.setPassword(this.password);
        _options.setDisableAutoDiscovery(true);

        GPUdb _dbc = new GPUdb(this.url, _options);
        testConnection(_dbc);
        return _dbc;
    }
    
    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(true);
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }

    /**
     * Used to test the kinetica connection.
     * @param _gpudb
     * @throws GPUdbException
     */
    public static void testConnection(GPUdb _gpudb) throws GPUdbException {
        final String _versionKey = "version.gpudb_core_version";
    
        GservControllerBase.LOG.info("Connecting to gpudb {}@{}", _gpudb.getUsername(), _gpudb.getURL().getHost());
        
        Map<String,String> _request = new HashMap<>();
        _request.put(ShowSystemPropertiesRequest.Options.PROPERTIES, _versionKey);
        ShowSystemPropertiesResponse _response = _gpudb.showSystemProperties(_request);
        
        String _version = _response.getPropertyMap().get(_versionKey);
        GservControllerBase.LOG.info("Connected to GPUdb: {}", _version);
    }
    
}
