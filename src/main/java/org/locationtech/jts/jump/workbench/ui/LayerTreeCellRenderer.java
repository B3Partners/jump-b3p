
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

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.locationtech.jts.jump.workbench.model.Layer;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.jump.workbench.model.Category;
import org.locationtech.jts.jump.workbench.model.LayerTreeModel;
import org.locationtech.jts.jump.workbench.model.Layerable;
import org.locationtech.jts.jump.workbench.ui.renderer.RenderingManager;

/**
 * Implements a {@link TreeCellRenderer} for the {@link
 * Layer Layer} tree.  This class may be
 * renamed to LayerRenderer in the future.
 */

public class LayerTreeCellRenderer implements TreeCellRenderer {
    //<<TODO:NAMING>> Rename class to LayerRenderer [Jon Aquino]
    private LayerNameRenderer layerNameRenderer = new LayerNameRenderer();
    private JLabel rootRendererComponent = new JLabel("Root");
    private DefaultTreeCellRenderer categoryRenderer = new DefaultTreeCellRenderer();

    public LayerTreeCellRenderer(RenderingManager renderingManager) {
        layerNameRenderer.setCheckBoxVisible(true);
        layerNameRenderer.setIndicatingEditability(true);
        layerNameRenderer.setIndicatingProgress(true, renderingManager);
    }

    public LayerNameRenderer getLayerNameRenderer() {
        return layerNameRenderer;
    }

    public Component getTreeCellRendererComponent(
        JTree tree,
        Object value,
        boolean selected,
        boolean expanded,
        boolean leaf,
        int row,
        boolean hasFocus) {
        Object node = value;
        if (node instanceof LayerTreeModel.Root) {
            return getTreeCellRendererComponent((LayerTreeModel.Root) node);
        }

        if (node instanceof Category) {
            categoryRenderer.setBackgroundNonSelectionColor(tree.getBackground());

            JLabel categoryRendererComponent =
                (JLabel) categoryRenderer.getTreeCellRendererComponent(
                    tree,
                    value,
                    selected,
                    expanded,
                    leaf,
                    row,
                    hasFocus);
            categoryRendererComponent.setFont(new JLabel().getFont().deriveFont(Font.BOLD));
            categoryRendererComponent.setText(((Category) node).getName());
            if (expanded) {
                categoryRendererComponent.setIcon(UIManager.getIcon("Tree.openIcon"));
            } else {
                categoryRendererComponent.setIcon(UIManager.getIcon("Tree.closedIcon"));
            }

            return categoryRendererComponent;
        }

        if (node instanceof Layerable) {
            return layerNameRenderer.getTreeCellRendererComponent(
                tree,
                value,
                selected,
                expanded,
                leaf,
                row,
                hasFocus);
        }

        Assert.shouldNeverReachHere(node.getClass().toString());

        return null;
    }

    private Component getTreeCellRendererComponent(LayerTreeModel.Root root) {
        return rootRendererComponent;
    }
}
