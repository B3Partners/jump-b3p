package org.locationtech.jts.jump.workbench.ui.plugin.clipboard;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;

import org.locationtech.jts.jump.workbench.plugin.EnableCheck;
import org.locationtech.jts.jump.workbench.plugin.EnableCheckFactory;
import org.locationtech.jts.jump.workbench.plugin.MultiEnableCheck;
import org.locationtech.jts.jump.workbench.plugin.PlugInContext;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.jump.workbench.WorkbenchContext;
import org.locationtech.jts.jump.workbench.ui.plugin.ExportImagePlugIn;

public class CopyImagePlugIn extends ExportImagePlugIn {
    public boolean execute(PlugInContext context) throws Exception {
        Transferable transferable = createTransferable(context);
        if (transferable == null) {
            context.getWorkbenchFrame()
                    .warnUser("Could not copy the image for some reason");
            return false;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(transferable, new DummyClipboardOwner());
        return true;
    }
    private Transferable createTransferable(final PlugInContext context) {
        return new AbstractTransferable(
                new DataFlavor[]{DataFlavor.imageFlavor}) {
            public Object getTransferData(DataFlavor flavor)
                    throws UnsupportedFlavorException, IOException {
                Assert.isTrue(flavor == DataFlavor.imageFlavor);
                return image(context.getLayerViewPanel());
            }
        };
    }
    public static MultiEnableCheck createEnableCheck(
            WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(
                workbenchContext);
        return new MultiEnableCheck()
                .add(checkFactory
                        .createWindowWithLayerViewPanelMustBeActiveCheck()).add(new EnableCheck() {
                    public String check(JComponent component) {
                        //Need Java 1.4's ability to auto-convert DataFlavor.imageFlavor to
                        //the native image format for the platform 
                        //(see http://access1.sun.com/tutorials/Swing_Tutorial/Dnd-Merlin-Tutorial/3.html).
                        //[Jon Aquino 11/6/2003]
                        return !java14OrNewer()
                                ? "This feature requires Java 1.4 or newer"
                                : null;
                    }
                });
    }
}