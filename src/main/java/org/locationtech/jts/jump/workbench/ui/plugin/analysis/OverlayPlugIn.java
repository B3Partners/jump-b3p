
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

package org.locationtech.jts.jump.workbench.ui.plugin.analysis;

import javax.swing.JComboBox;

import org.locationtech.jts.jump.workbench.model.StandardCategoryNames;
import org.locationtech.jts.jump.workbench.plugin.AbstractPlugIn;
import org.locationtech.jts.jump.workbench.plugin.PlugInContext;
import org.locationtech.jts.jump.workbench.plugin.ThreadedPlugIn;
import org.locationtech.jts.jump.feature.FeatureCollection;
import org.locationtech.jts.jump.feature.FeatureSchema;
import org.locationtech.jts.jump.task.TaskMonitor;
import org.locationtech.jts.jump.tools.AttributeMapping;
import org.locationtech.jts.jump.tools.OverlayEngine;
import org.locationtech.jts.jump.workbench.ui.GUIUtil;
import org.locationtech.jts.jump.workbench.ui.MultiInputDialog;
import org.locationtech.jts.jump.workbench.ui.images.IconLoader;

/**
 *
 * Creates a new layer containing intersections of all pairs of
 * features from two input layers.  Splits {@link
 * org.locationtech.jts.geom.MultiPolygon Multipolygons} and {@link
 * org.locationtech.jts.geom.GeometryCollection
 * GeometryCollections}, and filters out non-Polygons.
 */

public class OverlayPlugIn extends AbstractPlugIn implements ThreadedPlugIn {
    private final static String POLYGON_OUTPUT = "Limit output to Polygons only";
    private final static String FIRST_LAYER = "First layer";
    private final static String SECOND_LAYER = "Second layer";
    private final static String TRANSFER_ATTRIBUTES_FROM_FIRST_LAYER = "Transfer attributes from first layer";
    private final static String TRANSFER_ATTRIBUTES_FROM_SECOND_LAYER = "Transfer attributes from second layer";
    private MultiInputDialog dialog;
    private OverlayEngine overlayEngine;

    public OverlayPlugIn() {
    }

    public boolean execute(PlugInContext context) throws Exception {
        overlayEngine = prompt(context);

        return overlayEngine != null;
    }

    private OverlayEngine prompt(PlugInContext context) {
        //Unlike ValidatePlugIn, here we always call #initDialog because we want
        //to update the layer comboboxes. [Jon Aquino]
        initDialog(context);
        dialog.setVisible(true);

        if (!dialog.wasOKPressed()) {
            return null;
        }

        OverlayEngine e = new OverlayEngine();
        e.setAllowingPolygonsOnly(dialog.getBoolean(POLYGON_OUTPUT));
        e.setSplittingGeometryCollections(dialog.getBoolean(POLYGON_OUTPUT));

        return e;
    }

    private void initDialog(PlugInContext context) {
        dialog = new MultiInputDialog(context.getWorkbenchFrame(),
                getName(), true);
        dialog.setSideBarImage(IconLoader.icon("Overlay.gif"));
        dialog.setSideBarDescription(
            "Creates a new layer containing intersections " +
            "of all pairs of input features.");
        String fieldName = FIRST_LAYER;
        JComboBox addLayerComboBox = dialog.addLayerComboBox(fieldName, context.getCandidateLayer(0), null, context.getLayerManager());
        String fieldName1 = SECOND_LAYER;
        JComboBox addLayerComboBox1 = dialog.addLayerComboBox(fieldName1, context.getCandidateLayer(1), null, context.getLayerManager());
        dialog.addCheckBox(POLYGON_OUTPUT, true, "Splits MultiPolygons and GeometryCollections, and filters out non-Polygons");
        dialog.addCheckBox(TRANSFER_ATTRIBUTES_FROM_FIRST_LAYER,
            true);
        dialog.addCheckBox(TRANSFER_ATTRIBUTES_FROM_SECOND_LAYER,
            true);
        GUIUtil.centreOnWindow(dialog);
    }

    public void run(TaskMonitor monitor, PlugInContext context)
        throws Exception {
        FeatureCollection a = dialog.getLayer(FIRST_LAYER).getFeatureCollectionWrapper();
        FeatureCollection b = dialog.getLayer(SECOND_LAYER)
                                    .getFeatureCollectionWrapper();
        FeatureCollection overlay = overlayEngine.overlay(a, b, mapping(a, b),
                monitor);
        context.addLayer(StandardCategoryNames.WORKING, "Overlay", overlay);
    }

    private AttributeMapping mapping(FeatureCollection a, FeatureCollection b) {
        return new AttributeMapping(dialog.getBoolean(
                TRANSFER_ATTRIBUTES_FROM_FIRST_LAYER) ? a.getFeatureSchema()
                                                      : new FeatureSchema(),
            dialog.getBoolean(TRANSFER_ATTRIBUTES_FROM_SECOND_LAYER)
            ? b.getFeatureSchema() : new FeatureSchema());
    }
}
