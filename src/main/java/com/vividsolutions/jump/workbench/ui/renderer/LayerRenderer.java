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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;


public class LayerRenderer extends FeatureCollectionRenderer {
    private Layer layer;
    private LayerViewPanel panel;

    public LayerRenderer(final Layer layer, LayerViewPanel panel) {
        //Use layer as the contentID [Jon Aquino]
        super(layer, panel,
            new ImageCachingFeatureCollectionRenderer(layer, panel) {
                protected ThreadSafeImage getImage() {
                    if (!layer.isVisible()) {
                        return null;
                    }

                    return super.getImage();
                }

                public Runnable createRunnable() {
                    if (!layer.isVisible()) {
                        //If the cached image is null, leave it alone. [Jon Aquino]
                        return null;
                    }

                    return super.createRunnable();
                }
            });
        this.layer = layer;
        this.panel = panel;
    }

    protected Collection styles() {
        //new ArrayList to avoid ConcurrentModificationExceptions. [Jon Aquino]
        ArrayList styles = new ArrayList(layer.getStyles());
        styles.remove(layer.getVertexStyle());
        styles.remove(layer.getLabelStyle());

        //Move to last. [Jon Aquino]
        styles.add(layer.getVertexStyle());
        styles.add(layer.getLabelStyle());

        return styles;
    }

    protected Map layerToFeaturesMap() {
        Envelope viewportEnvelope = panel.getViewport()
                                         .getEnvelopeInModelCoordinates();

        return Collections.singletonMap(layer,
            layer.getFeatureCollectionWrapper().query(viewportEnvelope));
    }
}
