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

package com.vividsolutions.jump.workbench.ui.renderer;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Timer;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.OrderedMap;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

public class RenderingManager {
    private LayerViewPanel panel;
    private Map contentIDToRendererMap = new OrderedMap();
    private OrderedMap contentIDToLowRendererFactoryMap = new OrderedMap();
    private OrderedMap contentIDToHighRendererFactoryMap = new OrderedMap();

    //There's no performance advantage to rendering dozens of non-WMS layers in parallel.
    //In fact, it will make the GUI less responsive. [Jon Aquino]
    private ThreadQueue defaultRendererThreadQueue = new ThreadQueue(1);

    //WMS processing is done on the server side, so allow WMS queries to be done
    //in parallel. But not too many, as each Thread consumes 1 MB of memory
    //(see http://mindprod.com/jglossthread.html). The Threads may pile up if
    //the server is down. [Jon Aquino]
    private ThreadQueue wmsRendererThreadQueue = new ThreadQueue(20);

    //250 ms wasn't as good as 1 s because less got painted on each repaint,
    //making rendering appear to be slower. [Jon Aquino]
    private Timer repaintTimer = new Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            for (Iterator i = contentIDToRendererMap.values().iterator();
                i.hasNext();
                ) {
                Renderer renderer = (Renderer) i.next();
                if (renderer.isRendering()) {
                    repaintPanel();
                    return;
                }
            }

            repaintTimer.stop();
            repaintPanel();
        }
    });
    private boolean paintingEnabled = true;

    public RenderingManager(final LayerViewPanel panel) {
        this.panel = panel;
        repaintTimer.setCoalesce(true);
        putAboveLayerables(
            SelectionBackgroundRenderer.CONTENT_ID,
            new Renderer.Factory() {
            public Renderer create() {
                return new SelectionBackgroundRenderer(panel);
            }
        });
        putAboveLayerables(
            FeatureSelectionRenderer.CONTENT_ID,
            new Renderer.Factory() {
            public Renderer create() {
                return new FeatureSelectionRenderer(panel);
            }
        });
        putAboveLayerables(
            LineStringSelectionRenderer.CONTENT_ID,
            new Renderer.Factory() {
            public Renderer create() {
                return new LineStringSelectionRenderer(panel);
            }
        });
        putAboveLayerables(
            PartSelectionRenderer.CONTENT_ID,
            new Renderer.Factory() {
            public Renderer create() {
                return new PartSelectionRenderer(panel);
            }
        });
    }

    public void putBelowLayerables(
        Object contentID,
        Renderer.Factory factory) {
        contentIDToLowRendererFactoryMap.put(contentID, factory);
    }

    public void putAboveLayerables(
        Object contentID,
        Renderer.Factory factory) {
        contentIDToHighRendererFactoryMap.put(contentID, factory);
    }

    public void renderAll() {
        defaultRendererThreadQueue.clear();
        wmsRendererThreadQueue.clear();

        for (Iterator i = contentIDs().iterator(); i.hasNext();) {
            Object contentID = i.next();
            render(contentID);
        }
    }

    protected List contentIDs() {
        ArrayList contentIDs = new ArrayList();
        contentIDs.addAll(contentIDToLowRendererFactoryMap.keyList());
        for (Iterator i =
            panel.getLayerManager().reverseIterator(Layerable.class);
            i.hasNext();
            ) {
            Layerable layerable = (Layerable) i.next();
            contentIDs.add(layerable);
        }

        contentIDs.addAll(contentIDToHighRendererFactoryMap.keyList());

        return contentIDs;
    }

    public Renderer getRenderer(Object contentID) {
        return (Renderer) contentIDToRendererMap.get(contentID);
    }

    private void setRenderer(Object contentID, Renderer renderer) {
        contentIDToRendererMap.put(contentID, renderer);
    }

    public void render(Object contentID) {
        render(contentID, true);
    }

    public void render(Object contentID, boolean clearImageCache) {         
        
        if (getRenderer(contentID) == null) {
            setRenderer(contentID, createRenderer(contentID));
        }

        if (getRenderer(contentID).isRendering()) {
            getRenderer(contentID).cancel();

            //It might not cancel immediately, so create a new Renderer [Jon Aquino]
            setRenderer(contentID, createRenderer(contentID));
        }

        if (clearImageCache) {
            getRenderer(contentID).clearImageCache();
        }
        Runnable runnable = getRenderer(contentID).createRunnable();
        if (runnable != null) {
            //Before I would create threads that did nothing. Now I never do 
            //that -- I just return null. A dozen threads that do nothing make the 
            //system sluggish. [Jon Aquino]
            (
                (contentID instanceof WMSLayer)
                    ? wmsRendererThreadQueue
                    : defaultRendererThreadQueue).add(
                runnable);
        }

        if (!repaintTimer.isRunning()) {
            repaintPanel();
            repaintTimer.start();
        }
    }

    public void repaintPanel() {
        if (!paintingEnabled) {
            return;
        }

        panel.superRepaint();
    }

    protected Renderer createRenderer(Object contentID) {
        if (contentID instanceof Layer) {
            return new LayerRenderer((Layer) contentID, panel);
        }

        if (contentID instanceof WMSLayer) {
            return new WMSLayerRenderer((WMSLayer) contentID, panel);
        }

        if (contentIDToLowRendererFactoryMap.containsKey(contentID)) {
            return (
                (
                    Renderer
                        .Factory) contentIDToLowRendererFactoryMap
                        .get(
                    contentID))
                .create();
        }
        if (contentIDToHighRendererFactoryMap.containsKey(contentID)) {
            return (
                (
                    Renderer
                        .Factory) contentIDToHighRendererFactoryMap
                        .get(
                    contentID))
                .create();
        }
        Assert.shouldNeverReachHere();
        return null;
    }

    public void setPaintingEnabled(boolean paintingEnabled) {
        this.paintingEnabled = paintingEnabled;
    }

    public void copyTo(Graphics2D destination) {
        for (Iterator i = contentIDs().iterator(); i.hasNext();) {
            Object contentID = i.next();

            if (getRenderer(contentID) != null) {
                getRenderer(contentID).copyTo(destination);
            } 
        }
    }

    public ThreadQueue getDefaultRendererThreadQueue() {
        return defaultRendererThreadQueue;
    }

    public void dispose() {
        repaintTimer.stop();
        defaultRendererThreadQueue.dispose();
        wmsRendererThreadQueue.dispose();
        //The ThreadSafeImage cached in each Renderer consumes 1 MB of memory,
        //according to OptimizeIt [Jon Aquino]
        contentIDToRendererMap.clear();
    }
}
