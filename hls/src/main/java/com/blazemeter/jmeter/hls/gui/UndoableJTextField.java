package com.blazemeter.jmeter.hls.gui;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import java.awt.event.ActionEvent;
import java.util.TreeMap;

public class UndoableJTextField extends JTextField {
	private static final long serialVersionUID = -8629818843640232549L;
	private UndoManager currentUndoManager = null;
	private TreeMap undoableDocumentMap = new TreeMap();

	public UndoableJTextField() {
	}

	public UndoableJTextField(Document doc, String text, int columns) {
		super(doc, text, columns);
	}

	public UndoableJTextField(int columns) {
		super(columns);
	}

	public UndoableJTextField(String text) {
		super(text);
	}

	public UndoableJTextField(String text, int columns) {
		super(text, columns);
	}

	public void initActionMap(Object obj) {
		String actionMapKeyPrefix = obj.getClass().getName() + "_" + obj.hashCode();
		String undoActionKey = actionMapKeyPrefix + "_UNDO";
		String redoActionKey = actionMapKeyPrefix + "_REDO";
		this.getDocument().addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent e) {
				if (UndoableJTextField.this.currentUndoManager != null) {
					UndoableEdit ue = e.getEdit();
					UndoableJTextField.this.currentUndoManager.addEdit(ue);
				}

			}
		});
		InputMap im = this.getInputMap();
		ActionMap am = this.getActionMap();
		im.put(KeyStroke.getKeyStroke(90, 2, true), undoActionKey);
		am.put(undoActionKey, new AbstractAction() {
			private static final long serialVersionUID = -3929069112755477881L;

			public void actionPerformed(ActionEvent e) {
				if (UndoableJTextField.this.currentUndoManager != null
						&& UndoableJTextField.this.currentUndoManager.canUndo()) {
					try {
						UndoableJTextField.this.currentUndoManager.undo();
					} catch (CannotUndoException var3) {
						;
					}
				}

			}
		});
		im.put(KeyStroke.getKeyStroke(89, 2, true), redoActionKey);
		am.put(redoActionKey, new AbstractAction() {
			private static final long serialVersionUID = -5914985114920163371L;

			public void actionPerformed(ActionEvent e) {
				if (UndoableJTextField.this.currentUndoManager != null
						&& UndoableJTextField.this.currentUndoManager.canRedo()) {
					try {
						UndoableJTextField.this.currentUndoManager.redo();
					} catch (CannotUndoException var3) {
						;
					}
				}

			}
		});
	}

	public void switchDocument(Object container, Object keyObj, String contents) {
		this.currentUndoManager = null;
		String key = container.getClass().getName() + "_" + container.hashCode() + "_" + keyObj.getClass().getName()
				+ keyObj.hashCode();
		UndoableJTextField.UndoableDocument ud = (UndoableJTextField.UndoableDocument) this.undoableDocumentMap
				.get(key);
		if (ud == null) {
			ud = new UndoableJTextField.UndoableDocument();
			ud.undoManager = new UndoManager();
			ud.document = new PlainDocument();

			try {
				ud.document.insertString(0, contents, (AttributeSet) null);
			} catch (BadLocationException var7) {
				;
			}

			this.undoableDocumentMap.put(key, ud);
			ud.document.addUndoableEditListener(new UndoableEditListener() {
				public void undoableEditHappened(UndoableEditEvent e) {
					if (UndoableJTextField.this.currentUndoManager != null) {
						UndoableEdit ue = e.getEdit();
						UndoableJTextField.this.currentUndoManager.addEdit(ue);
					}

				}
			});
		}

		this.setDocument(ud.document);
		this.currentUndoManager = ud.undoManager;
	}

	public void clear() {
		this.currentUndoManager = null;
		this.setDocument(new PlainDocument());
	}

	public void clear(String text) {
		this.currentUndoManager = null;
		PlainDocument doc = new PlainDocument();

		try {
			doc.insertString(0, text, (AttributeSet) null);
		} catch (BadLocationException var4) {
			;
		}

		this.setDocument(doc);
	}

	public static class UndoableDocument {
		public Document document = null;
		public UndoManager undoManager = null;

		public UndoableDocument() {
		}
	}
}
