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

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JFileChooser;

import org.locationtech.jts.jump.workbench.plugin.PlugInContext;
import org.locationtech.jts.jump.workbench.plugin.ThreadedBasePlugIn;
import org.locationtech.jts.jump.coordsys.CoordinateSystemRegistry;
import org.locationtech.jts.jump.feature.FeatureCollection;
import org.locationtech.jts.jump.io.datasource.Connection;
import org.locationtech.jts.jump.io.datasource.DataSource;
import org.locationtech.jts.jump.task.TaskMonitor;
import org.locationtech.jts.jump.util.java2xml.XML2Java;
import org.locationtech.jts.jump.workbench.model.Category;
import org.locationtech.jts.jump.workbench.model.Layer;
import org.locationtech.jts.jump.workbench.model.LayerManager;
import org.locationtech.jts.jump.workbench.model.Layerable;
import org.locationtech.jts.jump.workbench.model.Task;
import org.locationtech.jts.jump.workbench.ui.GUIUtil;
import org.locationtech.jts.jump.workbench.ui.WorkbenchFrame;

public class OpenProjectPlugIn extends ThreadedBasePlugIn {
    private JFileChooser fileChooser;

    public OpenProjectPlugIn() {}

    public String getName() {
        return "Open Task";
    }

    public void initialize(PlugInContext context) throws Exception {
        //Don't initialize fileChooser in field declaration -- seems too early because
        //we sometimes get a WindowsFileChooserUI NullPointerException [Jon Aquino 12/10/2003]
        fileChooser = GUIUtil.createJFileChooserWithExistenceChecking();
        fileChooser.setDialogTitle("Open Task");
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        GUIUtil.removeChoosableFileFilters(fileChooser);
        fileChooser.addChoosableFileFilter(SaveProjectAsPlugIn.JUMP_PROJECT_FILE_FILTER);
        fileChooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
        fileChooser.setFileFilter(SaveProjectAsPlugIn.JUMP_PROJECT_FILE_FILTER);
    }

    public boolean execute(PlugInContext context) throws Exception {
        reportNothingToUndoYet(context);

        if (JFileChooser.APPROVE_OPTION
            != fileChooser.showOpenDialog(context.getWorkbenchFrame())) {
            return false;
        }

        return true;
    }

    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        open(fileChooser.getSelectedFile(), context.getWorkbenchFrame(), monitor);
    }

    private void open(
        File file,
        WorkbenchFrame workbenchFrame,
        final TaskMonitor monitor)
        throws Exception {
        monitor.report("Creating objects");

        FileReader reader = new FileReader(file);

        try {
            Task sourceTask = (Task) new XML2Java().read(reader, Task.class);
            //I can't remember why I'm creating a new Task instead of using
            //sourceTask. There must be a good reason. [Jon Aquino]
            Task newTask = new Task();
            newTask.setName(GUIUtil.nameWithoutExtension(file));
            newTask.setProjectFile(file);
            workbenchFrame.addTaskFrame(newTask);
            loadLayers(sourceTask.getLayerManager(), newTask.getLayerManager(), CoordinateSystemRegistry.instance(workbenchFrame.getContext().getBlackboard()), monitor);
        } finally {
            reader.close();
        }
    }

    private void loadLayers(
        LayerManager sourceLayerManager,
        LayerManager newLayerManager,
        CoordinateSystemRegistry registry,
        TaskMonitor monitor)
        throws Exception {
        for (Iterator i = sourceLayerManager.getCategories().iterator(); i.hasNext();) {
            Category sourceLayerCategory = (Category) i.next();
            //Explicitly add categories. Can't rely on LayerManager#addLayerable
            //to add the categories, because a category might not have any layers. [Jon Aquino]
            newLayerManager.addCategory(sourceLayerCategory.getName());

            //LayerManager#addLayerable adds layerables to the top. So reverse the order.
            //[Jon Aquino]
            ArrayList layerables = new ArrayList(sourceLayerCategory.getLayerables());
            Collections.reverse(layerables);

            for (Iterator j = layerables.iterator(); j.hasNext();) {
                Layerable layerable = (Layerable) j.next();
                monitor.report("Loading " + layerable.getName());
                layerable.setLayerManager(newLayerManager);

                if (layerable instanceof Layer) {
                    Layer layer = (Layer) layerable;
                    layer.setFeatureCollection(
                        executeQuery(
                            layer.getDataSourceQuery().getQuery(),
                            layer.getDataSourceQuery().getDataSource(), registry, monitor));
                    layer.setFeatureCollectionModified(false);                            
                }

                newLayerManager.addLayerable(sourceLayerCategory.getName(), layerable);
            }
        }
    }

    private FeatureCollection executeQuery(String query, DataSource dataSource, CoordinateSystemRegistry registry, TaskMonitor monitor)
        throws Exception {
        Connection connection = dataSource.getConnection();
        try {
            return dataSource.installCoordinateSystem(connection.executeQuery(query, monitor), registry);
        } finally {
            connection.close();
        }
    }
}
