/**
 * Action to press buttons for the purpose of hotkeys
 * Part of the Easy Loop program
 * 
 * @author David Hampson (DavidHampson.97@gmail.com)
 */

package easyLoop;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class PressButton extends AbstractAction {
	
	/**
	 * Serial ID to ensure version continuity
	 */
	private static final long serialVersionUID = 1L;
	
	JButton b;
	JCheckBox c;
	
	/**
	 * Initializes the button to press
	 * @param b button to press
	 */
	public PressButton (JButton b) {
		this.b = b;
	}
	
	/**
	 * Initializes the checkbox to press
	 * @param c check box to press
	 */
	public PressButton (JCheckBox c) {
		this.c = c;
	}
	
	/**
	 * An action of clicking the button
	 */
	public void actionPerformed(ActionEvent e) {
		if(b != null) b.doClick();
		else {
			if(c.isSelected()) c.setSelected(false);
			else c.setSelected(true);
		}
	}	
	
	/**
	 * Maps key to preform action on button
	 * 
	 * @param b button to map to
	 * @param key key to trigger action
	 * @param action action to preform
	 */
	static void setHotkey(JButton b, String key, String action) {
		b.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
		.put(KeyStroke.getKeyStroke(key), action);
		b.getActionMap().put(action, new PressButton(b));
	}

	/**
	 * Maps key to preform action on checkBox
	 * 
	 * @param c
	 * @param key key to trigger action
	 * @param action action to preform
	 */
	static void setHotkey(JCheckBox c, String key, String action) {
		c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
		.put(KeyStroke.getKeyStroke(key), action);
		c.getActionMap().put(action, new PressButton(c));
	}

}
