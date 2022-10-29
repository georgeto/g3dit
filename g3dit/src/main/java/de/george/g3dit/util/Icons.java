package de.george.g3dit.util;

import javax.swing.ImageIcon;

import com.jidesoft.icons.IconsFactory;

public class Icons {
	private Icons() {}

	public static class Action {
		public static final String ADD = "/icons/plus.png";
		public static final String DELETE = "/icons/delete.png";
		public static final String EDIT = "/icons/edit.png";
		public static final String CLONE = "/icons/clone.png";
		public static final String COPY = "/icons/copy.png";
		public static final String ERASE = "/icons/erase.png";
		public static final String BOOK = "/icons/book.png";
		public static final String BOOK_EDIT = "/icons/book-edit.png";
		public static final String LAYER_EDIT = "/icons/layer-pencil.png";
		public static final String FIND = "/icons/binocular.png";
		public static final String DIFF = "/icons/edit-diff.png";
	}

	public static class Signal {
		public static final String INFO = "/icons/info.png";
		public static final String WARN = "/icons/warn.png";
		public static final String ERROR = "/icons/error.png";
	}

	public static class Select {
		public static final String SELECT = "/icons/select.png";
		public static final String TICK = "/icons/tick.png";
		public static final String CANCEL = "/icons/cancel.png";
		public static final String CANCEL_EDIT = "/icons/cancel-edit.png";
		public static final String CHECK_BOX = "/icons/check-box.png";
	}

	public static class IO {
		public static final String OPEN = "/icons/open.png";
		public static final String SAVE = "/icons/save.png";
		public static final String SAVE_AS = "/icons/save-as.png";
		public static final String IMPORT = "/icons/import.png";
		public static final String UPLOAD = "/icons/drive-upload.png";
		public static final String FOLDER_EXPLORE = "/icons/folder-explore.png";
	}

	public static class Arrow {
		public static final String CURVE = "/icons/arrow-curve.png";
		public static final String UP = "/icons/arrow-up.png";
		public static final String DOWN = "/icons/arrow-down.png";
		public static final String RETURN_AFTER = "/icons/arrow-return-180.png";
		public static final String RETURN_BEFORE = "/icons/arrow-return-180-left.png";
		public static final String CIRCLE_DOUBLE = "/icons/arrow-circle-double.png";
	}

	public static class Data {
		public static final String THREED = "/icons/3d.png";
		public static final String CLASS = "/icons/class.png";
		public static final String CLASS_PLUS = "/icons/class-plus.png";
		public static final String CLASS_MINUS = "/icons/class-minus.png";
		public static final String NUMBER = "/icons/number.png";
		public static final String SORT = "/icons/sort.png";
		public static final String SETTINGS = "/icons/system-settings.png";
		public static final String COUNTER = "/icons/counter.png";
		public static final String COUNTER_RESET = "/icons/counter-reset.png";
		public static final String OPEN_BOOK = "/icons/stringtable.png";
		public static final String EDIT_BOX = "/icons/contentbox.png";
		public static final String INFORMATION = "/icons/information.png";
		public static final String LOG = "/icons/log.png";
		public static final String EXIT = "/icons/exit.png";
		public static final String CARD_EXPORT = "/icons/card-export.png";
		public static final String CARD_IMPORT = "/icons/card-import.png";
		public static final String CARD_MINUS = "/icons/card-minus.png";
		public static final String CARD_PLUS = "/icons/card-plus.png";
		public static final String TABLE_EXPORT = "/icons/table-export.png";
		public static final String TABLE_IMPORT = "/icons/table-import.png";
		public static final String TABLE_MINUS = "/icons/table-minus.png";
		public static final String TABLE_PLUS = "/icons/table-plus.png";
	}

	public static class Document {
		public static final String LETTER_L = "/icons/blue-document-attribute-l.png";
		public static final String LETTER_M = "/icons/blue-document-attribute-m.png";
		public static final String LETTER_N = "/icons/blue-document-attribute-n.png";
		public static final String LETTER_S = "/icons/blue-document-attribute-s.png";
		public static final String LETTER_T = "/icons/blue-document-attribute-t.png";
		public static final String LETTER_H = "/icons/blue-document-attribute-h.png";
		public static final String LETTER_I = "/icons/blue-document-attribute-i.png";
		public static final String LETTER_R = "/icons/blue-document-attribute-r.png";
		public static final String CONVERT = "/icons/blue-document-convert.png";
		public static final String EXPORT = "/icons/blue-document-export.png";
		public static final String PLUS = "/icons/blue-document-plus.png";
		public static final String TEMPLATE = "/icons/blue-document-template.png";
	}

	public static class Node {
		public static final String INSERT = "/icons/node-insert.png";
		public static final String INSERT_PREVIOUS = "/icons/node-insert-previous.png";
		public static final String INSERT_NEXT = "/icons/node-insert-next.png";
		public static final String INSERT_CHILD = "/icons/node-insert-child.png";
	}

	public static class Color {
		public static final String ARROW = "/icons/color-arrow.png";
		public static final String PENCIL = "/icons/color-pencil.png";
	}

	public static class Misc {
		public static final String TINY_HEXER = "/icons/tinyhexer.png";
		public static final String SCRIPT = "/icons/script-code-red.png";
		public static final String COOKIE = "/icons/cookie.png";
		public static final String COOKIE_BITE = "/icons/cookie-bite.png";
		public static final String COOKIE_CHOCO = "/icons/cookie-chocolate.png";
		public static final String COOKIE_CHOCO_SPRINKLES = "/icons/cookie-chocolate-sprinkles.png";
		public static final String GLOBE = "/icons/globe.png";
		public static final String GEOLOCATION = "/icons/geolocation.png";
		public static final String MAP = "/icons/map.png";
		public static final String CHEST = "/icons/chest.png";
		public static final String WAND_MAGIC = "/icons/wand-magic.png";
		public static final String CLIPBOARD = "/icons/clipboard-paste.png";
		public static final String CHAIN_MINUS = "/icons/chain-minus.png";
		public static final String MAGNIFIER = "/icons/magnifier.png";
		public static final String MAGNIFIER_WARN = "/icons/magnifier-warn.png";
	}

	public static class Flags {
		public static final String LANG = "/icons/flag.png";

		public static String forLanguage(String name) {
			return "/icons/flag-" + name + ".png";
		}
	}

	public static ImageIcon getDisabledImageIcon(String name) {
		if (name != null) {
			return IconsFactory.getDisabledImageIcon(Icons.class, name);
		} else {
			return null;
		}
	}

	public static ImageIcon getImageIcon(String name) {
		if (name != null) {
			return IconsFactory.getImageIcon(Icons.class, name);
		} else {
			return null;
		}
	}

	public static void main(String[] argv) {
		IconsFactory.generateHTML(Icons.class);
	}
}
