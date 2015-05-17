package com.vividsolutions.jump.workbench.ui.plugin;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.PrintStream;
import java.io.Reader;
import bsh.ConsoleInterface;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.util.JConsole;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxPlugIn;
public class BeanShellPlugIn extends ToolboxPlugIn {
	public void initialize(PlugInContext context) throws Exception {
		createMainMenuItem(new String[]{"View"}, null, context
				.getWorkbenchContext());
	}
	public String getName() {
		return "BeanShell";
	}
	protected void initializeToolbox(ToolboxDialog toolbox) {
		try {
			final JConsole console = new JConsole();
			console.setPreferredSize(new Dimension(430, 240));
			console
					.print("The WorkbenchContext may be referred to as \"wc\".\n");
			console
					.print("Warning: Pasting in multiple statements may cause the application to freeze. Try wrapping them in a function.\n");
			toolbox.getCenterPanel().add(console, BorderLayout.CENTER);
			Interpreter interpreter = new Interpreter(console);
			interpreter.set("wc", toolbox.getContext());
			interpreter.eval("setAccessibility(true)");
			new Thread(interpreter).start();
		} catch (EvalError e) {
			toolbox.getContext().getErrorHandler().handleThrowable(e);
		}
	}
}
