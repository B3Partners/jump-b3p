
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

import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.jump.workbench.model.Layer;
import org.locationtech.jts.jump.workbench.plugin.AbstractPlugIn;
import org.locationtech.jts.jump.workbench.plugin.PlugInContext;
import org.locationtech.jts.jump.feature.Feature;
import org.locationtech.jts.jump.workbench.ui.HTMLFrame;


/**
 * Computes various statistics for selected layers.
 */
public class LayerStatisticsPlugIn extends AbstractPlugIn {
    public LayerStatisticsPlugIn() {
    }

    public boolean execute(PlugInContext context) throws Exception {
        //Call #getSelectedLayers before #clear, because #clear will surface
        //output window. [Jon Aquino]
        Layer[] selectedLayers = context.getSelectedLayers();
        HTMLFrame out = context.getOutputFrame();
        out.createNewDocument();
        out.addHeader(1, "Layer Statistics");

        LayerStatistics totalStats = new LayerStatistics();
        Envelope totalEnv = new Envelope();

        for (int i = 0; i < selectedLayers.length; i++) {
            Layer layer = selectedLayers[i];
            LayerStatistics ls = layerStatistics(layer, totalStats);

            out.addHeader(2, "Layer: " + layer.getName());

            Envelope layerEnv = layer.getFeatureCollectionWrapper().getEnvelope();
            out.addField("Envelope:", layerEnv.toString());
            totalEnv.expandToInclude(layerEnv);
            output(ls, out);
        }

        if (selectedLayers.length > 1) {
            out.addHeader(2, "Summary for all Layers");
            out.addField("Envelope:", totalEnv.toString());
            output(totalStats, out);
        }
        out.surface();

        return true;
    }

    private LayerStatistics layerStatistics(final Layer layer,
        LayerStatistics totalStats) {
        LayerStatistics ls = new LayerStatistics();

        for (Iterator i = layer.getFeatureCollectionWrapper().iterator(); i.hasNext();) {
            Feature f = (Feature) i.next();
            Geometry g = f.getGeometry();
            double area = g.getArea();
            double length = g.getLength();

            // these both need work - need to recurse into geometries
            int comps = 1;

            if (g instanceof GeometryCollection) {
                comps = ((GeometryCollection) g).getNumGeometries();
            }

            Coordinate[] pts = g.getCoordinates();
            int holes = 0;

            if (g instanceof Polygon) {
                holes = ((Polygon) g).getNumInteriorRing();
            }

            ls.addFeature(pts.length, holes, comps, area, length);
            totalStats.addFeature(pts.length, holes, comps, area, length);
        }

        return ls;
    }

    public void output(LayerStatistics ls, HTMLFrame out) {
        //=========  Output  ===============
        out.addField("# Features:", ls.featureCount + "");

        out.append("<table border='1'>");
        out.append(
            "<tr><td bgcolor=#CCCCCC>&nbsp;</td><td  bgcolor=#CCCCCC align='center'> Min </td><td  bgcolor=#CCCCCC align='center'> Max </td><td  bgcolor=#CCCCCC align='center'> Avg </td><td  bgcolor=#CCCCCC align='center'> Total </td></tr>");
        out.append("<tr><td bgcolor=#CCCCCC> Pts </td><td align='right'>" +
            ls.minCoord + "</td><td align='right'>" + ls.maxCoord +
            "</td><td align='right'>" + ls.avgCoord() +
            "</td><td align='right'>" + ls.totalCoord + "</td></tr>");
        out.append("<tr><td bgcolor=#CCCCCC> Holes </td><td align='right'>" +
            ls.minHoles + "</td><td align='right'>" + ls.maxHoles +
            "</td><td align='right'>" + ls.avgHoles() +
            "</td><td align='right'>" + ls.totalHoles + "</td></tr>");
        out.append(
            "<tr><td bgcolor=#CCCCCC> Components </td><td align='right'>" +
            ls.minComp + "</td><td align='right'>" + ls.maxComp +
            "</td><td align='right'>" + ls.avgComp() +
            "</td><td align='right'>" + ls.totalComp + "</td></tr>");
        out.append("<tr><td bgcolor=#CCCCCC> Area </td><td align='right'>" +
            ls.minArea + "</td><td align='right'>" + ls.maxArea +
            "</td><td align='right'>" + ls.avgArea() +
            "</td><td align='right'>" + ls.totalArea + "</td></tr>");
        out.append("<tr><td bgcolor=#CCCCCC> Length </td><td align='right'>" +
            ls.minLength + "</td><td align='right'>" + ls.maxLength +
            "</td><td align='right'>" + ls.avgLength() +
            "</td><td align='right'>" + ls.totalLength + "</td></tr>");
        out.append("</table>");
    }

    public class LayerStatistics {
        boolean isFirst = true;
        int minCoord = 0;
        int maxCoord = 0;
        int totalCoord = 0;
        int minComp = 0;
        int maxComp = 0;
        int totalComp = 0;
        int minHoles = 0;
        int maxHoles = 0;
        int totalHoles = 0;
        double minArea = 0.0;
        double maxArea = 0.0;
        double totalArea = 0.0;
        double minLength = 0.0;
        double maxLength = 0.0;
        double totalLength = 0.0;
        int featureCount = 0;

        public void addFeature(int coordCount, int holeCount, int compCount,
            double area, double length) {
            featureCount++;

            if (isFirst || (coordCount < minCoord)) {
                minCoord = coordCount;
            }

            if (isFirst || (coordCount > maxCoord)) {
                maxCoord = coordCount;
            }

            totalCoord += coordCount;

            if (isFirst || (holeCount < minHoles)) {
                minHoles = holeCount;
            }

            if (isFirst || (holeCount > maxHoles)) {
                maxHoles = holeCount;
            }

            totalHoles += holeCount;

            if (isFirst || (compCount < minComp)) {
                minComp = compCount;
            }

            if (isFirst || (compCount > maxComp)) {
                maxComp = compCount;
            }

            totalComp += compCount;

            if (isFirst || (area < minArea)) {
                minArea = area;
            }

            if (isFirst || (area > maxArea)) {
                maxArea = area;
            }

            totalArea += area;

            if (isFirst || (length < minLength)) {
                minLength = length;
            }

            if (isFirst || (length > maxLength)) {
                maxLength = length;
            }

            totalLength += length;

            isFirst = false;
        }

        public double avgCoord() {
            return (featureCount == 0) ? 0.0 : (totalCoord / featureCount);
        }

        public double avgHoles() {
            return (featureCount == 0) ? 0.0 : (totalHoles / featureCount);
        }

        public double avgComp() {
            return (featureCount == 0) ? 0.0 : (totalComp / featureCount);
        }

        public double avgArea() {
            return (featureCount == 0) ? 0.0 : (totalArea / featureCount);
        }

        public double avgLength() {
            return (featureCount == 0) ? 0.0 : (totalLength / featureCount);
        }
    }
}
