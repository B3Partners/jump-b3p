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

import javax.swing.JInternalFrame;

import org.locationtech.jts.jump.workbench.plugin.AbstractPlugIn;
import org.locationtech.jts.jump.workbench.plugin.PlugInContext;
import org.locationtech.jts.jump.workbench.ui.EditOptionsPanel;
import org.locationtech.jts.jump.workbench.ui.GUIUtil;
import org.locationtech.jts.jump.workbench.ui.LayerViewPanelProxy;
import org.locationtech.jts.jump.workbench.ui.OptionsDialog;
import org.locationtech.jts.jump.workbench.ui.SnapVerticesToolsOptionsPanel;
import org.locationtech.jts.jump.workbench.ui.snap.GridRenderer;
import org.locationtech.jts.jump.workbench.ui.images.IconLoader;

public class OptionsPlugIn extends AbstractPlugIn {

    public boolean execute(PlugInContext context) throws Exception {
        reportNothingToUndoYet(context);
        GUIUtil.centreOnWindow(dialog(context));
        dialog(context).setVisible(true);
        if (dialog(context).wasOKPressed()) {
            JInternalFrame[] frames = context.getWorkbenchFrame().getInternalFrames();
            for (int i = 0; i < frames.length; i++) {
                if (frames[i] instanceof LayerViewPanelProxy) {
                    ((LayerViewPanelProxy) frames[i])
                        .getLayerViewPanel()
                        .getRenderingManager()
                        .render(
                        GridRenderer.CONTENT_ID,
                        true);
                }
            }
        }
        return dialog(context).wasOKPressed();
    }
    private OptionsDialog dialog(PlugInContext context) {
        return OptionsDialog.instance(context.getWorkbenchContext().getWorkbench());
    }
    
    public void initialize(PlugInContext context) throws Exception {
        dialog(context)
                .addTab("View / Edit", new EditOptionsPanel(context
                        .getWorkbenchContext().getWorkbench().getBlackboard(), context
                        .getWorkbenchContext().getWorkbench().getFrame()
                        .getDesktopPane()))        ;
        dialog(context)
                .addTab("Snap Vertices Tools", GUIUtil.resize(
                        IconLoader.icon("QuickSnap.gif"), 16),
                        new SnapVerticesToolsOptionsPanel(context
                                .getWorkbenchContext().getWorkbench()
                                .getBlackboard()))        ;
    }

}
