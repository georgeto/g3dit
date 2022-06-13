package de.george.g3dit.util;

import java.awt.Window;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.base.Joiner;
import com.teamunify.i18n.I;

import de.george.g3utils.structure.Stringtable;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.DefaultClass;
import de.george.lrentnode.classes.G3Class;

public class StringtableHelper {
	public static void clearStringtableSafe(List<? extends G3ClassContainer> classContainers, Stringtable stringtable) {
		clearStringtableSafe(classContainers, stringtable, false, null);
	}

	public static void clearStringtableSafe(Collection<? extends G3ClassContainer> classContainers, Stringtable stringtable,
			boolean displayWarning, Window parent) {
		Set<String> unknownClasses = classContainers.stream().flatMap(c -> c.getClasses().stream()).filter(c -> c instanceof DefaultClass)
				.filter(c -> ((DefaultClass) c).getRaw() != null && ((DefaultClass) c).getRaw().length > 2).map(G3Class::getClassName)
				.collect(Collectors.toSet());

		if (!unknownClasses.isEmpty()) {
			if (displayWarning) {
				boolean isConfirmed = TaskDialogs.isConfirmed(parent, I.tr(
						"The file file contains unknown classes, by cleaning up the stringtable\ncould delete string table entries that are still used by those classes.\nClean up the string table anyway?"),
						Joiner.on("\n").join(unknownClasses));
				if (isConfirmed) {
					stringtable.clear();
				}
			}
		} else {
			stringtable.clear();
		}
	}
}
