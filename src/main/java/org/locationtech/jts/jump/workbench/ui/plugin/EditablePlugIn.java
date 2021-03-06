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

package org.locationtech.jts.jump.workbench.ui.plugin;

import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;

import org.locationtech.jts.jump.workbench.model.Layer;
import org.locationtech.jts.jump.workbench.ui.cursortool.editing.EditingPlugIn;
import org.locationtech.jts.jump.workbench.WorkbenchContext;
import org.locationtech.jts.jump.workbench.plugin.AbstractPlugIn;
import org.locationtech.jts.jump.workbench.plugin.EnableCheck;
import org.locationtech.jts.jump.workbench.plugin.EnableCheckFactory;
import org.locationtech.jts.jump.workbench.plugin.MultiEnableCheck;
import org.locationtech.jts.jump.workbench.plugin.PlugInContext;

public class EditablePlugIn extends AbstractPlugIn {
   
    private EditingPlugIn editingPlugIn;

    public boolean execute(PlugInContext context) throws Exception {
        reportNothingToUndoYet(context);
        boolean makeEditable = !context.getSelectedLayer(0).isEditable();
        for (Iterator i = Arrays.asList(context.getSelectedLayers()).iterator(); i.hasNext(); ) {
            Layer selectedLayer = (Layer) i.next();
            selectedLayer.setEditable(makeEditable);
        }
        if (makeEditable && !editingPlugIn.getToolbox(context.getWorkbenchContext()).isVisible()) {
            editingPlugIn.execute(context);
        }
        return true;
    }

    public EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck()
            .add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck())
            .add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1))
            .add(new EnableCheck() {
            public String check(JComponent component) {
                ((JCheckBoxMenuItem) component).setSelected(
                    workbenchContext.createPlugInContext().getSelectedLayer(0).isEditable());
                return null;
            }
        });
    }

    public EditablePlugIn(EditingPlugIn editingPlugIn) {
        this.editingPlugIn = editingPlugIn;
    }

}
