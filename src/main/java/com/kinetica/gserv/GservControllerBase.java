/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 */

package com.kinetica.gserv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kinetica.gvis.GvisConfigTerrain;
import com.kinetica.gvis.attribute.AttributeCache;
import com.kinetica.gvis.render.BaseRenderer;

public class GservControllerBase {
    
    static final Logger LOG = LoggerFactory.getLogger(GservControllerBase.class);
    //private static final String MODEL_GLTF_JSON = "model/gltf+json";

    /**
     * Return content as a glTF attachment.
     * @param renderer
     * @return
     * @throws Exception
     */
    protected static ResponseEntity<byte[]> responseGltf(BaseRenderer renderer) throws Exception {
        byte[] gltfBytes = renderer.writeGltf();
        String contentDisposition = String.format("attachment; filename=%s", renderer.getFilename());
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
            .body(gltfBytes);
    }
    
    protected final GvisConfigTerrain _gvisConfig;
    
    @Autowired 
    private ObjectMapper mapper;
    
    public GservControllerBase(GservConfiguration _config) throws Exception {
        this._gvisConfig = _config.getConfig();
        //_gvisConfig._serviceCacheEnabled = true;
        
        // force init of attributes
        AttributeCache.getInstance(this._gvisConfig._dbc);
    }

    /**
     * Error handler for service. This will format the exception as json.
     * @param _req
     * @param _ex
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleBadRequest(HttpServletRequest _req, Exception _ex) {
        LOG.error(_ex.getMessage(), _ex);
        
        List<String> _formattedStack = new ArrayList<>();
        for(StackTraceElement _ste: _ex.getStackTrace()) {
            _formattedStack.add(_ste.toString());
        }

        Map<String,Object> _map = new HashMap<>();
        _map.put("type", _ex.getClass().getName());
        _map.put("message", _ex.getMessage());
        _map.put("stack", _formattedStack);
        
        String _json = null;
        try {
            _json = this.mapper.writeValueAsString(_map);
        }
        catch (JsonProcessingException e) {
            _json = e.getMessage();
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<String>(_json, headers, HttpStatus.BAD_REQUEST);
    }
    
}
