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
package org.locationtech.jts.jump.workbench.ui.plugin.clipboard;

import org.locationtech.jts.geom.Coordinate;

import org.locationtech.jts.jump.workbench.plugin.AbstractPlugIn;
import org.locationtech.jts.jump.workbench.plugin.EnableCheckFactory;
import org.locationtech.jts.jump.workbench.plugin.MultiEnableCheck;
import org.locationtech.jts.jump.workbench.plugin.PlugInContext;
import org.locationtech.jts.jump.workbench.WorkbenchContext;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.IOException;


public class CopyThisCoordinatePlugIn extends AbstractPlugIn {
    public boolean execute(PlugInContext context) throws Exception {
        final Coordinate c =
            context.getLayerViewPanel().getViewport().toModelCoordinate(
                context.getLayerViewPanel().getLastClickedPoint());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
            new AbstractTransferable(
                new DataFlavor[] { DataFlavor.plainTextFlavor }) {
                public Object getTransferData(DataFlavor flavor)
                    throws UnsupportedFlavorException, IOException {
                    return c.x + ", " + c.y;
                }
            }, new DummyClipboardOwner());

        return true;
    }

    public static MultiEnableCheck createEnableCheck(
        WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory =
            new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(
            checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
    }
}