package com.vividsolutions.jump.workbench.ui.plugin;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.java2xml.Java2XML;
import com.vividsolutions.jump.util.java2xml.XML2Java;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;


public class PersistentBlackboardPlugIn extends AbstractPlugIn {
    private static final String FILENAME = "workbench-state.xml";
    private static final String BLACKBOARD_KEY = PersistentBlackboardPlugIn.class.getName() +
        " - BLACKBOARD";

    public static Blackboard get(WorkbenchContext context) {
        return (Blackboard) context.getBlackboard().get(BLACKBOARD_KEY, new Blackboard());
    }

    public void initialize(final PlugInContext context)
        throws Exception {
        restoreState(context.getWorkbenchContext());
        context.getWorkbenchFrame().addComponentListener(new ComponentAdapter() {
                public void componentHidden(ComponentEvent e) {
                    saveState(context.getWorkbenchContext());
                }
            });

    }

    private void restoreState(WorkbenchContext workbenchContext) {
        try {
            FileReader fileReader = new FileReader(FILENAME);

            try {
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                try {
                    get(workbenchContext).putAll(((Blackboard) new XML2Java().read(
                            bufferedReader, Blackboard.class)).getProperties());
                } finally {
                    bufferedReader.close();
                }
            } finally {
                fileReader.close();
            }
        } catch (Exception e) {
            //Eat it. Persistence isn't critical. [Jon Aquino]
        }
    }

    private void saveState(WorkbenchContext workbenchContext) {
        try {
            FileWriter fileWriter = new FileWriter(FILENAME, false);

            try {
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                try {
                    new Java2XML().write(get(workbenchContext),
                        "workbench-state", bufferedWriter);
                    bufferedWriter.flush();
                    fileWriter.flush();
                } finally {
                    bufferedWriter.close();
                }
            } finally {
                fileWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            //Eat it. Persistence isn't critical. [Jon Aquino]
        }
    }

}
