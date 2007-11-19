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
package com.vividsolutions.jump.workbench.datasource;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.vividsolutions.jts.util.Assert;

import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;

/**
 * Prompts the user to pick a dataset to save.
 * @see DataSourceQueryChooserDialog
 */
public class SaveDatasetAsPlugIn extends ThreadedBasePlugIn {
    private static String LAST_FORMAT_KEY = SaveDatasetAsPlugIn.class.getName() +
        " - LAST FORMAT";

    private DataSourceQueryChooserDialog getDialog(PlugInContext context) {
        String KEY = getClass().getName() + " - DIALOG";
        if (null == context.getWorkbenchContext().getWorkbench().getBlackboard()
                               .get(KEY)) {
            context.getWorkbenchContext().getWorkbench().getBlackboard().put(KEY,
                new DataSourceQueryChooserDialog(DataSourceQueryChooserManager.get(
                        context.getWorkbenchContext().getWorkbench()
                               .getBlackboard()).getSaveDataSourceQueryChoosers(),
                    context.getWorkbenchFrame(), getName(), true));
        }

        return (DataSourceQueryChooserDialog) context.getWorkbenchContext()
                                                     .getWorkbench()
                                                     .getBlackboard().get(KEY);
    }

    public void initialize(final PlugInContext context) throws Exception {
        //Give other plug-ins a chance to add DataSourceQueryChoosers
        //before the dialog is realized. [Jon Aquino]
        context.getWorkbenchFrame().addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                String format = (String) PersistentBlackboardPlugIn.get(context.getWorkbenchContext())
                                                                   .get(LAST_FORMAT_KEY);
                if (format != null) {
                    getDialog(context).setSelectedFormat(format);
                }
            }
        });        
    }

    public boolean execute(PlugInContext context) throws Exception {
        GUIUtil.centreOnWindow(getDialog(context));
        getDialog(context).setVisible(true);
        if (getDialog(context).wasOKPressed()) {
            PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).put(LAST_FORMAT_KEY,
			getDialog(context).getSelectedFormat());
        }

        return getDialog(context).wasOKPressed();
    }

    public void run(TaskMonitor monitor, PlugInContext context)
        throws Exception {
        Assert.isTrue(getDialog(context).getCurrentChooser()
                          .getDataSourceQueries().size() == 1);

        DataSourceQuery dataSourceQuery = (DataSourceQuery) getDialog(context)
                                                                .getCurrentChooser()
                                                                .getDataSourceQueries()
                                                                .iterator()
                                                                .next();
        Assert.isTrue(dataSourceQuery.getDataSource().isWritable());
        monitor.report("Saving " + dataSourceQuery.toString() + "...");

        Connection connection = dataSourceQuery.getDataSource().getConnection();
        try {
            connection.executeUpdate(dataSourceQuery.getQuery(),
                context.getSelectedLayer(0).getFeatureCollectionWrapper(),
                monitor);
        } finally {
            connection.close();
        }
        context.getSelectedLayer(0).setDataSourceQuery(dataSourceQuery)
               .setFeatureCollectionModified(false);
    }

    public static MultiEnableCheck createEnableCheck(
        final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);

        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck())
                                     .add(checkFactory.createExactlyNLayersMustBeSelectedCheck(
                1));
    }
}
