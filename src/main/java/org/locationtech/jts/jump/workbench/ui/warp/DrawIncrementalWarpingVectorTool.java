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

package org.locationtech.jts.jump.workbench.ui.warp;

import java.awt.Cursor;
import java.awt.geom.NoninvertibleTransformException;

import javax.swing.Icon;

import org.locationtech.jts.jump.workbench.model.AbstractVectorLayerFinder;
import org.locationtech.jts.jump.workbench.model.LayerManagerProxy;
import org.locationtech.jts.jump.workbench.model.UndoableCommand;
import org.locationtech.jts.jump.workbench.ui.cursortool.VectorTool;
import org.locationtech.jts.jump.workbench.ui.images.IconLoader;

public class DrawIncrementalWarpingVectorTool extends VectorTool {

    public DrawIncrementalWarpingVectorTool(WarpingPanel warpingPanel) {
        setColor(IncrementalWarpingVectorLayerFinder.COLOR);
        this.warpingPanel = warpingPanel;
    }
    
    private WarpingPanel warpingPanel;

    protected AbstractVectorLayerFinder createVectorLayerFinder(LayerManagerProxy layerManagerProxy) {
        return new IncrementalWarpingVectorLayerFinder(layerManagerProxy);
    }

    public Icon getIcon() {
        return IconLoader.icon("GreenVectorToolBar.gif");
    }

    public Cursor getCursor() {
        return createCursor(IconLoader.icon("GreenVectorCursor.gif").getImage());
    }
    
    protected UndoableCommand createCommand() throws NoninvertibleTransformException {
        return warpingPanel.addWarping(warpingPanel.addWarpingVectorGeneration(super.createCommand()));
    }

}