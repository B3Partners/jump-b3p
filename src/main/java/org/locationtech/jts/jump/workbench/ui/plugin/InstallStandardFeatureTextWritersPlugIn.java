package org.locationtech.jts.jump.workbench.ui.plugin;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.jump.workbench.plugin.AbstractPlugIn;
import org.locationtech.jts.jump.workbench.plugin.PlugInContext;
import org.locationtech.jts.jump.feature.Feature;
import org.locationtech.jts.jump.io.GMLGeometryWriter;
import org.locationtech.jts.jump.util.Fmt;
import org.locationtech.jts.jump.util.StringUtil;
import org.locationtech.jts.jump.workbench.ui.AbstractFeatureTextWriter;

public class InstallStandardFeatureTextWritersPlugIn extends AbstractPlugIn {

    public void initialize(PlugInContext context) throws Exception {
        context.getWorkbenchContext().getFeatureTextWriterRegistry().register(
                WKT_WRITER);
        context.getWorkbenchContext().getFeatureTextWriterRegistry().register(
                GML_WRITER);
        context.getWorkbenchContext().getFeatureTextWriterRegistry().register(
                COORDINATE_WRITER);
    }

    private static final AbstractFeatureTextWriter COORDINATE_WRITER = new AbstractFeatureTextWriter(
            false, "CL", "Coordinate List") {
        public String write(Feature feature) {
            StringBuffer s = new StringBuffer();
            String className = StringUtil.classNameWithoutQualifiers(feature
                    .getGeometry().getClass().getName());
            s.append(className + "\n");
            Coordinate[] coordinates = feature.getGeometry().getCoordinates();
            for (int i = 0; i < coordinates.length; i++) {
                s.append("[" + Fmt.fmt(i, 10) + "] ");
                s.append(coordinates[i].x + ", " + coordinates[i].y + "\n");
            }
            return s.toString().trim();
        }
    };

    private static final AbstractFeatureTextWriter GML_WRITER = new AbstractFeatureTextWriter(
            false, "GML", "Geography Markup Language") {
        public String write(Feature feature) {
            return writer.write(feature.getGeometry());
        }

        private GMLGeometryWriter writer = new GMLGeometryWriter();
    };

    private static final AbstractFeatureTextWriter WKT_WRITER = new AbstractFeatureTextWriter(
            true, "WKT", "Well-Known Text") {
        public String write(Feature feature) {
            return wktWriter.write(feature.getGeometry()).trim();
        }

        private WKTWriter wktWriter = new WKTWriter();
    };
}
