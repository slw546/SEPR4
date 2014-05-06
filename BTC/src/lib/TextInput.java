package lib;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class TextInput implements lib.jog.input.EventHandler {

	public interface Accept {
		public boolean acceptString(String match);
	}
	
	private int x;
	private int y;
	private int width;
	private int height;
	private Accept accepter;

	private boolean selected;
	private double cursorTimer;
	private boolean cursorShown;
	private double cursorBlinkDelay = 0.6;
	private int charWidth = 8;
	
	private String text;
	private int cursorLocation;
	private boolean valid;
	
	public boolean active;
	
	public TextInput(int x, int y, int width, int height, Accept accepter, String initialText) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.accepter = accepter;
		this.text = initialText;
		this.cursorLocation = text.length();
		this.valid = accepter.acceptString(initialText);
		this.selected = false;
		this.cursorTimer = 0;
		this.cursorShown = true;
		this.active = true;
	}
	
	public TextInput(int x, int y, int width, int height, Accept accepter) {
		this(x, y, width, height, accepter, "");
	}
	
	public void update(double dt) {
		if (!selected) return;
		cursorTimer += dt;
		if (cursorTimer >= cursorBlinkDelay) {
			cursorTimer -= cursorBlinkDelay;
			cursorShown = !cursorShown;
		}
	}

	@Override
	public void mousePressed(int key, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(int key, int mx, int my) {
		if (!active) return;
		selected = (mx >= x && mx <= x + width && my >= y && my <= y + height);
		if (selected) {
			int newLocation = (mx - (x + 4)) / charWidth; 
			cursorLocation = Math.max(0, Math.min(text.length(), newLocation));
		}
	}

	@Override
	public void keyPressed(int key) {
		if (!active) return;
		if (!selected) return;
		boolean paste = (key == lib.jog.input.KEY_V && lib.jog.input.isKeyDown(lib.jog.input.KEY_LCRTL))
					 || (key == lib.jog.input.KEY_INSERT && lib.jog.input.isKeyDown(lib.jog.input.KEY_LSHIFT))
				 	 || (key == lib.jog.input.KEY_INSERT && lib.jog.input.isKeyDown(lib.jog.input.KEY_RSHIFT));
		boolean alphanumeric = (key >= lib.jog.input.KEY_1 && key <= lib.jog.input.KEY_FORWARD_SLASH)
							&& (key != lib.jog.input.KEY_TAB)
							&& (key != lib.jog.input.KEY_BACKSPACE)
							&& (key != lib.jog.input.KEY_RETURN)
							&& (key != lib.jog.input.KEY_LCRTL)
							&& (key != lib.jog.input.KEY_LSHIFT);
		if (paste) {
			Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
		    Transferable t = c.getContents(this);
	    	try {
				String clipboard = (String) t.getTransferData(DataFlavor.stringFlavor);
				addString(clipboard);
			} catch (UnsupportedFlavorException | IOException e) {}
		} else if (key == lib.jog.input.KEY_BACKSPACE) {
			backspace();
		} else if (key == lib.jog.input.KEY_DELETE) {
			delete();
		} else if (key == lib.jog.input.KEY_RIGHT) {
			moveCursor(1);
		} else if (key == lib.jog.input.KEY_LEFT) {
			moveCursor(-1);
		} else if (key == lib.jog.input.KEY_HOME) {
			cursorLocation = 0;
		} else if (key == lib.jog.input.KEY_END) {
			cursorLocation = text.length();
		} else if (alphanumeric) {
			addString(keyToString(key));
		}
	}
	
	private void moveCursor(int move) {
		if (cursorLocation + move >= 0 && cursorLocation + move <= text.length()) {
			cursorLocation += move;
		}
	}
	
	private void backspace() {
		if (cursorLocation == 0) return;
		String post = text.substring(cursorLocation);
		String pre = text.substring(0, cursorLocation - 1);
		text = pre + post;
		cursorLocation --;
		checkValid();
	}
	
	private void delete() {
		if (cursorLocation == text.length()) return;
		String post = text.substring(cursorLocation + 1);
		String pre = text.substring(0, cursorLocation);
		text = pre + post;
		checkValid();
	}
	
	private void addString(String addition) {
		String post = text.substring(cursorLocation);
		String pre = text.substring(0, cursorLocation);
		text = pre + addition + post;
		cursorLocation += addition.length();
		checkValid();
	}
	
	private void checkValid() {
		valid = accepter.acceptString(text);
	}

	@Override
	public void keyReleased(int key) { }
	
	public void draw(double size) {
		if (!active) return;
		lib.jog.graphics.rectangle(false, x, y, width, height);
		if (valid) {
			lib.jog.graphics.setColour(128, 128, 128);
		} else {
			lib.jog.graphics.setColour(128, 32, 32);
		}
		int textY = y + (int) ((height - 8*size) / 2);
		lib.jog.graphics.print(text, x + 4, textY, size);
		if (selected && cursorShown) {
			lib.jog.graphics.rectangle(true, x + cursorLocation * size * charWidth + 4, y + 8, 2, height - 16);
		}
	}
	
	private String keyToString(int key) {
		if (key >= lib.jog.input.KEY_1 && key <= lib.jog.input.KEY_0) {
			return String.valueOf((key - 1) % 10);
		} else if (key == lib.jog.input.KEY_FULL_STOP) {
			return ".";
		} else if (key == lib.jog.input.KEY_SEMICOLON && lib.jog.input.isKeyDown(lib.jog.input.KEY_LSHIFT) 
			    || key == lib.jog.input.KEY_SEMICOLON && lib.jog.input.isKeyDown(lib.jog.input.KEY_RSHIFT)) {
			return ":";
		} else {
			return "";
		}
	}

	public boolean isValid() {
		return active && valid;
	}
	
	public String getText() {
		return text;
	}
	
}
