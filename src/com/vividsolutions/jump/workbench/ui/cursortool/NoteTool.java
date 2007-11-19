package com.vividsolutions.jump.workbench.ui.cursortool;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.swing.*;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import com.vividsolutions.jump.workbench.ui.snap.SnapToFeaturesPolicy;

//To do: check Notes layer can be saved with a task
//[Jon Aquino 2004-03-10]
public class NoteTool extends NClickTool {

    private abstract class Mode {

        private Feature noteFeature;

        public Mode(Feature noteFeature) {
            this.noteFeature = noteFeature;
        }

        public Coordinate location() {
            return noteFeature.getGeometry().getCoordinate();
        }

        public abstract void commit(String text);

        protected Feature getNoteFeature() {
            return noteFeature;
        }

        public abstract String initialText();
    }

    private class CreateMode extends Mode {

        public CreateMode(final Coordinate location) {
            super(new BasicFeature(layer().getFeatureCollectionWrapper()
                    .getFeatureSchema()) {

                {
                    setAttribute("CREATED", new Date());
                    setAttribute("GEOMETRY", new GeometryFactory()
                            .createPoint(location));
                }
            });
        }

        public void commit(String text) {
            if (text.length() > 0) {
                disableAutomaticInitialZooming();
                getNoteFeature().setAttribute("MODIFIED", new Date());
                getNoteFeature().setAttribute("TEXT", text);
                EditTransaction transaction = new EditTransaction(
                        Collections.EMPTY_LIST, getName(), layer(),
                        isRollingBackInvalidEdits(), true, getPanel());
                transaction.createFeature(getNoteFeature());
                transaction.commit();
            }
        }

        public String initialText() {
            return "";
        }
    }

    private class EditMode extends Mode {

        public EditMode(Feature noteFeature) {
            super(noteFeature);
        }

        public void commit(final String text) {
            final Date modifiedDate = new Date();
            final Date oldModifiedDate = (Date) getNoteFeature().getAttribute(
                    "MODIFIED");
            final String oldText = getNoteFeature().getString("TEXT");
            execute(new UndoableCommand(getName()) {

                public void execute() {
                    update(getNoteFeature(), text, modifiedDate, layer());
                }

                public void unexecute() {
                    update(getNoteFeature(), oldText, oldModifiedDate, layer());
                }
            });
        }

        private void update(Feature noteFeature, String text,
                Date modifiedDate, Layer layer) {
            noteFeature.setAttribute("MODIFIED", modifiedDate);
            noteFeature.setAttribute("TEXT", text);
            layer.getLayerManager().fireFeaturesChanged(
                    Collections.singleton(noteFeature),
                    FeatureEventType.ATTRIBUTES_MODIFIED, layer);
        }

        public String initialText() {
            return getNoteFeature().getString("TEXT");
        }
    }

    public NoteTool() {
        super(1);
        getSnapManager().addPolicies(
                Collections.singleton(new SnapToFeaturesPolicy()));
        textArea = createTextArea();
        textArea.addFocusListener(new FocusAdapter() {

            public void focusLost(FocusEvent e) {
                removeTextAreaFromPanel();
            }
        });
    }

    private JTextArea textArea;

    private Mode mode;

    public void deactivate() {
        removeTextAreaFromPanel();
        super.deactivate();
    }

    private static JTextArea createTextArea() {
        return new JTextArea() {

            {
                setFont(new JLabel().getFont());
                setLineWrap(true);
                setWrapStyleWord(true);
                setBorder(BorderFactory.createLineBorder(Color.lightGray));
            }
        };
    }

    public Cursor getCursor() {
        return cursor;
    }

    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();
        Feature noteFeatureAtClick = noteFeature(getModelDestination());
        try {
            if (panelContainsTextArea() && noteFeatureAtClick == null) { return; }
        } finally {
            removeTextAreaFromPanel();
        }
        mode = mode(noteFeatureAtClick, getModelDestination());
        //Ensure #addTextAreaToPanel is called *after* all the
        //#removeTextAreaFromPanel calls [Jon Aquino 2004-03-05]
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    addTextAreaToPanel(mode.location());
                } catch (NoninvertibleTransformException e) {
                    getPanel().getContext().handleThrowable(e);
                }
            }
        });
    }

    private Mode mode(Feature noteFeatureAtClick, Coordinate click) {
        return noteFeatureAtClick == null ? (Mode) new CreateMode(click)
                : new EditMode(noteFeatureAtClick);
    }

    private Feature noteFeature(Coordinate click) {
        return noteFeature(new Envelope(click, new Coordinate(click.x - WIDTH
                / scale(), click.y + HEIGHT / scale())));
    }

    private Feature noteFeature(Envelope envelope) {
        return (Feature) firstOrNull(layer().getFeatureCollectionWrapper()
                .query(envelope));
    }

    private Object firstOrNull(Collection items) {
        return !items.isEmpty() ? items.iterator().next() : null;
    }

    private double scale() {
        return getPanel().getViewport().getScale();
    }

    private void addTextAreaToPanel(Coordinate location)
            throws NoninvertibleTransformException {
        layer().setVisible(true);
        if (getPanel().getLayout() != null) {
            getPanel().setLayout(null);
        }
        textArea.setText(mode.initialText());
        textArea.setBackground(layer().getBasicStyle().getFillColor());
        getPanel().add(textArea);
        textArea.setBounds((int) getPanel().getViewport().toViewPoint(location)
                .getX(), (int) getPanel().getViewport().toViewPoint(location)
                .getY(), WIDTH, HEIGHT);
        textArea.requestFocus();
    }

    private boolean panelContainsTextArea() {
        return Arrays.asList(getPanel().getComponents()).contains(textArea);
    }

    private void removeTextAreaFromPanel() {
        if (!panelContainsTextArea()) { return; }
        mode.commit(textArea.getText().trim());
        getPanel().remove(textArea);
        getPanel().superRepaint();
    }

    private void disableAutomaticInitialZooming() {
        getPanel().setViewportInitialized(true);
    }

    private static final int WIDTH = 80;

    private static final int HEIGHT = 30;

    private Layer layer() {
        final String name = "Notes";
        if (getPanel().getLayerManager().getLayer(name) != null) { return getPanel()
                .getLayerManager().getLayer(name); }
        return new Layer(name, Color.yellow.brighter().brighter(),
                new FeatureDataset(new FeatureSchema() {

                    {
                        addAttribute("CREATED", AttributeType.DATE);
                        addAttribute("MODIFIED", AttributeType.DATE);
                        addAttribute("TEXT", AttributeType.STRING);
                        addAttribute("GEOMETRY", AttributeType.GEOMETRY);
                    }
                }), getPanel().getLayerManager()) {

            {
                getLayerManager().deferFiringEvents(new Runnable() {

                    public void run() {
                        getBasicStyle().setAlpha(150);
                        addStyle(createStyle());
                        setDrawingLast(true);
                    }
                });
                getLayerManager().addLayer(StandardCategoryNames.SYSTEM, this);
            }

            private Style createStyle() {
                return new NoteStyle();
            }
        };
    }

    public Icon getIcon() {
        return icon;
    }

    public static class NoteStyle implements Style {

        private JTextArea myTextArea = createTextArea();

        private Layer layer;

        public void paint(Feature f, Graphics2D g, Viewport viewport)
                throws Exception {
            paint(f, viewport.toViewPoint(f.getGeometry().getCoordinate()), g);
        }

        private void paint(Feature f, Point2D location, Graphics2D g) {
            myTextArea.setText(f.getString("TEXT"));
            myTextArea.setBounds(0, 0, WIDTH, HEIGHT);
            Composite originalComposite = g.getComposite();
            g.translate(location.getX(), location.getY());
            try {
                g.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, layer.getBasicStyle()
                                .getAlpha() / 255f));
                myTextArea.paint(g);
            } finally {
                g.setComposite(originalComposite);
                g.translate(-location.getX(), -location.getY());
            }
        }

        public void initialize(Layer layer) {
            this.layer = layer;
            myTextArea.setBackground(layer.getBasicStyle().getFillColor());
        }

        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                Assert.shouldNeverReachHere();
                return null;
            }
        }

        public void setEnabled(boolean enabled) {
            throw new UnsupportedOperationException();
        }

        public boolean isEnabled() {
            return true;
        }

    }

    protected Shape getShape() throws NoninvertibleTransformException {
        return null;
    }

    private ImageIcon icon = IconLoader.icon("sticky.png");

    private Cursor cursor = GUIUtil.createCursorFromIcon(icon.getImage());

}
