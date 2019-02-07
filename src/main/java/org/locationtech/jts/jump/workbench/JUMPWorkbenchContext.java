
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

package org.locationtech.jts.jump.workbench;

import javax.swing.JInternalFrame;


import org.locationtech.jts.jump.util.Blackboard;
import org.locationtech.jts.jump.workbench.driver.DriverManager;
import org.locationtech.jts.jump.workbench.model.LayerManager;
import org.locationtech.jts.jump.workbench.model.LayerManagerProxy;
import org.locationtech.jts.jump.workbench.model.Task;
import org.locationtech.jts.jump.workbench.ui.ErrorHandler;
import org.locationtech.jts.jump.workbench.ui.LayerNamePanel;
import org.locationtech.jts.jump.workbench.ui.LayerNamePanelProxy;
import org.locationtech.jts.jump.workbench.ui.LayerViewPanel;
import org.locationtech.jts.jump.workbench.ui.LayerViewPanelProxy;
import org.locationtech.jts.jump.workbench.ui.TaskFrame;


/**
 * Implementation of {@link WorkbenchContext} for the {@link
 * JUMPWorkbench}.
 */
public class JUMPWorkbenchContext extends WorkbenchContext {
    private JUMPWorkbench workbench;

    public JUMPWorkbenchContext(JUMPWorkbench workbench) {
        this.workbench = workbench;
    }

    public JUMPWorkbench getWorkbench() {
        return workbench;
    }
    
    public Blackboard getBlackboard() {
		return workbench.getBlackboard();
	}

    public DriverManager getDriverManager() {
        return workbench.getDriverManager();
    }

    public ErrorHandler getErrorHandler() {
        return workbench.getFrame();
    }

    public Task getTask() {
        if (!(activeInternalFrame() instanceof TaskFrame)) {
            return null;
        }

        return ((TaskFrame) activeInternalFrame()).getTask();
    }

    public LayerNamePanel getLayerNamePanel() {
        if (!(activeInternalFrame() instanceof LayerNamePanelProxy)) {
            return null;
        }

        return ((LayerNamePanelProxy) activeInternalFrame()).getLayerNamePanel();
    }

    public LayerManager getLayerManager() {
        if (!(activeInternalFrame() instanceof LayerManagerProxy)) {
            //WarpingPanel assumes that this method returns null if the active frame is not
            //a LayerManagerProxy. [Jon Aquino]            
            return null;
        }

        return ((LayerManagerProxy) activeInternalFrame()).getLayerManager();
    }

    public LayerViewPanel getLayerViewPanel() {
        if (!(activeInternalFrame() instanceof LayerViewPanelProxy)) {
            return null;
        }

        return ((LayerViewPanelProxy) activeInternalFrame()).getLayerViewPanel();
    }

    private JInternalFrame activeInternalFrame() {
        return workbench.getFrame().getActiveInternalFrame();
    }
}
