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

package org.locationtech.jts.jump.workbench.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.jump.feature.Feature;
import org.locationtech.jts.jump.workbench.model.Layer;
import org.locationtech.jts.jump.workbench.ui.renderer.FeatureSelectionRenderer;

/**
* A collection of selected {@link Feature Features}
*/
public class FeatureSelection extends AbstractSelection {

    public List items(Geometry geometry) {
        ArrayList items = new ArrayList();
        items.add(geometry);
        return items;
    }    
    
    public FeatureSelection(SelectionManager selectionManager) {
        super(selectionManager);
    }    
    
    public String getRendererContentID() {
        return FeatureSelectionRenderer.CONTENT_ID;
    }

    protected boolean selectedInAncestors(Layer layer, Feature feature, Geometry item) {
        Assert.isTrue(getParent() == null);
        return false;
    }

    protected void unselectInDescendants(Layer layer, Feature feature, Collection items) {
        Assert.isTrue(getChild() instanceof PartSelection);    
        Assert.isTrue(getChild().getChild() instanceof LineStringSelection);            
        getChild().unselectItems(layer, feature);
        getChild().getChild().unselectItems(layer, feature);
    }

}
