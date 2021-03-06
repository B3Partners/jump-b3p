
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

package org.locationtech.jts.jump.workbench.ui.renderer.style;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.jump.workbench.model.Layer;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.jump.feature.Feature;
import org.locationtech.jts.jump.workbench.ui.GUIUtil;
import org.locationtech.jts.jump.workbench.ui.Viewport;


public abstract class VertexStyle implements Style {
    protected RectangularShape shape;
    protected int size = 4;
    private Color fillColor;
    private boolean enabled = false;

    protected VertexStyle(RectangularShape shape) {
        this.shape = shape;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void initialize(Layer layer) {
        //Set the vertices' fill color to the layer's line color
        fillColor = GUIUtil.alphaColor(layer.getBasicStyle().getLineColor(),
                layer.getBasicStyle().getAlpha());
    }

    public void paint(Feature f, Graphics2D g, Viewport viewport)
        throws Exception {
        Coordinate[] coordinates = f.getGeometry().getCoordinates();
        g.setColor(fillColor);

        for (int i = 0; i < coordinates.length; i++) {
            if (!viewport.getEnvelopeInModelCoordinates().contains(coordinates[i])) {
                //Otherwise get "sun.dc.pr.PRException: endPath: bad path" exception [Jon Aquino 10/22/2003]
                continue;
            }            
            paint(g,
                viewport.toViewPoint(
                    new Point2D.Double(coordinates[i].x, coordinates[i].y)));
        }
    }

    public void paint(Graphics2D g, Point2D p) {
        setFrame(p);
        render(g);
    }

    private void setFrame(Point2D p) {
        shape.setFrame(p.getX() - (getSize() / 2d),
            p.getY() - (getSize() / 2d), getSize(), getSize());
    }

    protected void render(Graphics2D g) {
        g.fill(shape);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            Assert.shouldNeverReachHere();

            return null;
        }
    }
}
