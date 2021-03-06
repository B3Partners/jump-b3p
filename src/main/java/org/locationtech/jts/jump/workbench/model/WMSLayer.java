/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package org.locationtech.jts.jump.workbench.model;

import java.awt.Image;
import java.awt.MediaTracker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.jump.util.Blackboard;
import org.locationtech.jts.jump.workbench.ui.LayerViewPanel;
import org.locationtech.jts.wms.BoundingBox;
import org.locationtech.jts.wms.MapRequest;
import org.locationtech.jts.wms.WMService;

/**
 * A Layerable that retrieves images from a Web Map Server.
 */
public class WMSLayer extends AbstractLayerable implements Cloneable {
    private String format;
    private List layerNames = new ArrayList();
    private String srs;
    private int alpha = 255;
    private WMService service;

    /**
     *  Called by Java2XML
     */
    public WMSLayer() {
    }

    public WMSLayer(LayerManager layerManager, String serverURL, String srs,
            List layerNames, String format) throws IOException {
        this(layerManager, initializedService(serverURL), srs, layerNames, format);
    }
    
    private static WMService initializedService(String serverURL) throws IOException {
        WMService initializedService = new WMService(serverURL);
        initializedService.initialize();
        return initializedService;
    }

    public WMSLayer(LayerManager layerManager, WMService initializedService, String srs,
        List layerNames, String format) throws IOException {   
        super((String) layerNames.get(0), layerManager);
        setService(initializedService);
        setSRS(srs);
        this.layerNames = new ArrayList(layerNames);
        setFormat(format);
    }

    private void setService(WMService service) {
        this.service = service;
        this.serverURL = service.getServerUrl();
    }

    public int getAlpha() {
        return alpha;
    }

    /**
     * @param alpha 0-255 (255 is opaque)
     */
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public Image createImage(LayerViewPanel panel) throws IOException {
        Image image = createRequest(panel).getImage();
        MediaTracker mt = new MediaTracker(new JButton());
        mt.addImage(image, 0);

        try {
            mt.waitForID(0);
        } catch (InterruptedException e) {
            Assert.shouldNeverReachHere();
        }

        return image;
    }

    private BoundingBox toBoundingBox(String srs, Envelope e) {
        return new BoundingBox(srs, (float) e.getMinX(), (float) e.getMinY(),
            (float) e.getMaxX(), (float) e.getMaxY());
    }

    public MapRequest createRequest(LayerViewPanel panel)
        throws IOException {
        MapRequest request = getService().createMapRequest();
        request.setBoundingBox(toBoundingBox(srs,
                panel.getViewport().getEnvelopeInModelCoordinates()));
        request.setFormat(format);
        request.setImageWidth(panel.getWidth());
        request.setImageHeight(panel.getHeight());
        request.setLayers(layerNames);
        request.setTransparent(true);

        return request;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void addLayerName(String layerName) {
        layerNames.add(layerName);
    }

    public List getLayerNames() {
        return Collections.unmodifiableList(layerNames);
    }

    public void setSRS(String srs) {
        this.srs = srs;
    }

    public String getSRS() {
        return srs;
    }

    public Object clone() throws java.lang.CloneNotSupportedException {
        WMSLayer clone = (WMSLayer) super.clone();
        clone.layerNames = new ArrayList(this.layerNames);

        return clone;
    }

    public void removeAllLayerNames() {
        layerNames.clear();
    }
    
    private Blackboard blackboard = new Blackboard();
    private String serverURL;

    public Blackboard getBlackboard() {
        return blackboard;
    }

    public WMService getService() throws IOException {
        if (service == null) {
            Assert.isTrue(serverURL != null);
            setService(initializedService(serverURL));
        }
        return service;
    }

    public String getServerURL() {
        //Called by Java2XML [Jon Aquino 2004-02-23]
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        //Called by Java2XML [Jon Aquino 2004-02-23]
        this.serverURL = serverURL;
    }
}
