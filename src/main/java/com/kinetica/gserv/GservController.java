/* 
 * Copyright (c) 2022, Chad Juliano, Kinetica DB Inc.
 */

package com.kinetica.gserv;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gpudb.GPUdbException;
import com.kinetica.gvis.rect.WGS84Rect;
import com.kinetica.gvis.render.PipeRenderer;
import com.kinetica.gvis.render.SphereRenderer;
import com.kinetica.gvis.render.TerrainRenderer;

import io.github.chadj2.mesh.MeshGltfWriter.AlphaMode;
import io.micrometer.core.annotation.Timed;

/**
 * Implementation of 3d tile service.
 * @author Chad Juliano
 */
@RestController
@CrossOrigin
public class GservController extends GservControllerBase {
    
    private static final Logger LOG = LoggerFactory.getLogger(GservController.class);
    
    @Autowired 
    public GservController(GservConfiguration config) throws Exception {
        super(config);
    }

    /**
     * Test connection to Kinetica.
     * @return
     * @throws GPUdbException
     */
    @RequestMapping(value="/status", method = RequestMethod.GET)
    public String getStatus() throws GPUdbException {
        GservConfiguration.testConnection(this._gvisConfig._dbc);
        return "OK";
    }
    
    @GetMapping(path = "/getSphereEventsJson", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getSphereEventsJson(
            @RequestParam(value="projectId", required=true) String projectId,
            @RequestParam(value="startTs", required=false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTs,
            @RequestParam(value="endTs", required=false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTs,
            @RequestParam(value="startOpacity", required=false) Double opacityStart,
            @RequestParam(value="endOpacity", required=false) Double opacityEnd,
            @RequestParam(value="adjustAmpScale", required=false) Double adjustAmpScale,
            @RequestParam(value="adjustAmpMean", required=false) Double adjustAmpMean,
            @RequestParam(value="adjustAmpSlope", required=false) Double adjustAmpSlope
            ) throws Exception 
    {
        LOG.info("getSphereEventsJson: projectId=<{}> dates=<{} to {}>", projectId, startTs, endTs);
        
        Instant startInst = null;
        if(startTs != null) {
            startInst = startTs.toInstant(ZoneOffset.UTC);
        }
        
        Instant endInst = null;
        if(endTs != null) {
            endInst = endTs.toInstant(ZoneOffset.UTC);
        }

        SphereRenderer renderer = new SphereRenderer(this._gvisConfig, projectId);
        renderer.setOpacity(opacityStart, opacityEnd);
        renderer.setTimeRange(startInst, endInst);
        renderer.adjustAmpMean(adjustAmpMean);
        renderer.adjustAmpSlope(adjustAmpSlope);
        renderer.adjustAmpScale(adjustAmpScale);
        
        return renderer.getJson();
    }
    
    /**
     * Render event spheres.
     * @param projectId
     * @return
     * @throws Exception
     */
    @GetMapping(path = "/getSphereEvents", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Timed(value = "get.sphere.events", longTask = true)
    public ResponseEntity<byte[]> getSphereEvents(
            @RequestParam(value="projectId", required=true) String projectId,
            @RequestParam(value="startTs", required=false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTs,
            @RequestParam(value="endTs", required=false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTs,
            @RequestParam(value="startOpacity", required=false) Double opacityStart,
            @RequestParam(value="endOpacity", required=false) Double opacityEnd,
            @RequestParam(value="adjustAmpScale", required=false) Double adjustAmpScale,
            @RequestParam(value="adjustAmpMean", required=false) Double adjustAmpMean,
            @RequestParam(value="adjustAmpSlope", required=false) Double adjustAmpSlope,
            @RequestParam(value="enableInstancing", required=false) Boolean enableInstancing
            ) throws Exception 
    {
        LOG.info("getSphereEvents: projectId=<{}> dates=<{} to {}>", projectId, startTs, endTs);
        
        Instant startInst = null;
        if(startTs != null) {
            startInst = startTs.toInstant(ZoneOffset.UTC);
        }
        
        Instant endInst = null;
        if(endTs != null) {
            endInst = endTs.toInstant(ZoneOffset.UTC);
        }

        SphereRenderer renderer = new SphereRenderer(this._gvisConfig, projectId);
        renderer.setOpacity(opacityStart, opacityEnd);
        renderer.setTimeRange(startInst, endInst);
        renderer.adjustAmpMean(adjustAmpMean);
        renderer.adjustAmpSlope(adjustAmpSlope);
        renderer.adjustAmpScale(adjustAmpScale);
        renderer.enableInstancing(enableInstancing);
        renderer.build();
        
        return responseGltf(renderer);
    }
    
    /**
     * Return a gltf model with a 3D pipe.
     * @param projectId
     * @param name
     * @return
     * @throws Exception
     */
    @GetMapping(path = "/getPipe", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Timed(value = "get.pipe", longTask = true)
    public ResponseEntity<byte[]> getPipe(
            @RequestParam(value="projectId", required=true) String projectId,
            @RequestParam(value="name", required=true) String name) throws Exception
    {
        LOG.info("getPipe: projectId=<{}> name<{}>", projectId, name);
        
        PipeRenderer renderer = new PipeRenderer(this._gvisConfig, projectId, name);
        renderer.build();
        return responseGltf(renderer);
    }
    
    /**
     * Get a terrain layer.
     * @param projectId
     * @param layer
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @param colorStr
     * @param opacity
     * @return
     * @throws Exception
     */
    @GetMapping(path = "/getTerrain", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
    @Timed(value = "get.terrain", longTask = true)
    public ResponseEntity<byte[]> getTerrain(
        @RequestParam(value="projectId", required=true) String projectId,
        @RequestParam(value="layer", required=true) int layer,
        @RequestParam(value="lat1", required=true) double lat1,
        @RequestParam(value="lon1", required=true) double lon1,
        @RequestParam(value="lat2", required=true) double lat2,
        @RequestParam(value="lon2", required=true) double lon2,
        @RequestParam(value="color", required=false) String colorStr,
        @RequestParam(value="opacity", required=false) Float opacity
        )  throws Exception
    {
        WGS84Rect rectWgs = WGS84Rect.fromBounds(lon1, lat1, lon2, lat2);
        LOG.info("getTerrain: {}", rectWgs.formatBounds());
        
        this._gvisConfig._enableTerrainProjections = false;
        this._gvisConfig._mapScaleXY = 1;
        
        TerrainRenderer renderer = new TerrainRenderer(this._gvisConfig, projectId);
        renderer.setAlphaMode(AlphaMode.BLEND_DS);
        renderer.enableTexture(false);
        renderer.setAttribute("z");
        renderer.setWgsRect(rectWgs);
        renderer.setColor(colorStr, opacity);
        renderer.addLayer(layer);
        
        return responseGltf(renderer);
    }
}
