package de.george.g3dit.gui.edit;

import java.awt.Component;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JComponent;

import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ui.ValidationGroup;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import de.george.g3dit.gui.edit.adapter.PropertyAdapter;
import de.george.g3dit.gui.edit.handler.PropertyHandler;
import de.george.g3dit.gui.table.TableColumnDef;
import de.george.lrentnode.archive.eCEntity;

public class PropertyPanelDef {
	private final PropertyAdapter<?> adapter;
	private final PropertyHandler<?> handler;
	private final TableColumnDef[] tableColumns;
	private final String name;
	private final String title;
	private final String tooltip;
	private final String[] valueList;
	private final ValidationGroup validation;
	private final Validator<?>[] validators;
	private final Predicate<eCEntity> hideIf;
	private final String constraints;
	private final Consumer<?> customizer;

	public PropertyPanelDef(PropertyAdapter<?> adapter, PropertyHandler<?> handler, TableColumnDef[] tableColumns, String name,
			String title, String tooltip, String[] valueList, ValidationGroup validation, Validator<?>[] validators,
			Predicate<eCEntity> hideIf, String constraints, Consumer<?> customizer) {
		this.adapter = adapter;
		this.handler = handler;
		this.tableColumns = tableColumns;
		this.name = name;
		this.title = title;
		this.tooltip = tooltip;
		this.valueList = valueList;
		this.validation = validation;
		this.validators = validators;
		this.hideIf = hideIf;
		this.constraints = constraints;
		this.customizer = customizer;
	}

	public boolean hasAdapter() {
		return adapter != null;
	}

	public PropertyAdapter<?> getAdapter() {
		return adapter;
	}

	public boolean hasHandler() {
		return handler != null;
	}

	public PropertyHandler<?> getHandler() {
		return handler;
	}

	public boolean hasTableCoumns() {
		return tableColumns != null;
	}

	public TableColumnDef[] getTableCoumns() {
		return tableColumns;
	}

	public String getName() {
		return name;
	}

	public boolean hasTitle() {
		return title != null;
	}

	public String getTitle() {
		return title;
	}

	public String getTooltip() {
		return tooltip;
	}

	public boolean hasValueList() {
		return valueList != null;
	}

	public String[] getValueList() {
		return valueList;
	}

	public ValidationGroup getValidation() {
		return validation;
	}

	public boolean hasValidators() {
		return validators != null && validators.length > 0;
	}

	public Validator<?>[] getValidators() {
		return validators;
	}

	@SuppressWarnings("unchecked")
	public void initValidation(Component comp) {
		if (hasValidators()) {
			comp.setName(getName());
			validation.add(comp, (Validator[]) validators);
		}
	}

	public boolean isHidden(eCEntity entity) {
		return hideIf != null && hideIf.test(entity);
	}

	public String getConstraints() {
		return constraints;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public <C> void customize(C data) {
		if (customizer != null) {
			((Consumer) customizer).accept(data);
		}
	}

	public void apply(JComponent comp) {
		comp.setToolTipText(tooltip);
		initValidation(comp);
		customize(comp);
	}

	public static final <T extends PropertyPanelBase<T>> Builder<T> with(PropertyPanelBase<T> panel) {
		return new Builder<>(panel);
	}

	public static final class Builder<T extends PropertyPanelBase<T>> extends PropertyPanelBuilderContext<Builder<T>, T> {
		private PropertyAdapter<?> adapter;
		private PropertyHandler<?> handler;
		private TableColumnDef[] tableColumns;
		private String name;
		private String title;
		private String tooltip;
		private String[] valueList;
		private ValidationGroup validation;
		private Validator<?>[] validators;
		private Predicate<eCEntity> hideIf;
		private String constraints = "spanx";
		private Consumer<?> customizer;

		public Builder(PropertyPanelBase<T> base) {
			super(base);
		}

		/**
		 * @param adapter Property for this entry.
		 */
		protected Builder<T> adapter(PropertyAdapter<?> adapter) {
			if (!Objects.isNull(handler)) {
				throw new IllegalStateException();
			}
			this.adapter = adapter;
			return this;
		}

		/**
		 * @param handler Handler for this entry.
		 */
		protected Builder<T> handler(PropertyHandler<?> handler) {
			if (!Objects.isNull(adapter)) {
				throw new IllegalStateException();
			}
			this.handler = handler;
			return this;
		}

		public Builder<T> tableColumns(TableColumnDef... tableColumns) {
			this.tableColumns = tableColumns;
			return this;
		}

		/**
		 * @param displayName Name under which the property is presented.
		 */
		public Builder<T> name(String name) {
			this.name = name;
			if (title == null) {
				title = name;
			}
			return this;
		}

		public Builder<T> title(String title) {
			this.title = title;
			return this;
		}

		public Builder<T> noTitle() {
			return title(null);
		}

		/**
		 * @param displayName Tooltip shown for the property.
		 */
		public Builder<T> tooltip(String tooltip) {
			this.tooltip = tooltip;
			return this;
		}

		public Builder<T> valueList(String... valueList) {
			this.valueList = valueList;
			return this;
		}

		/**
		 * @param displayName Tooltip shown for the property.
		 */
		public Builder<T> validate(ValidationGroup validation, Validator<?>... validators) {
			this.validation = validation;
			this.validators = validators;
			return this;
		}

		public Builder<T> hideIf(Predicate<eCEntity> hideIf) {
			this.hideIf = hideIf;
			return this;
		}

		public Builder<T> constraints(String constraints) {
			this.constraints = join(this.constraints, Strings.emptyToNull(constraints));
			return this;
		}

		public Builder<T> horizontalStart() {
			return horizontalStart(1);
		}

		public Builder<T> horizontalStart(int count) {
			constraints = base.newline();
			if (count > 1) {
				constraints("spanx " + count);
			}
			return this;
		}

		public Builder<T> horizontal() {
			return horizontal(1);
		}

		public Builder<T> horizontal(int count) {
			constraints = "gapleft 15px";
			if (count == -1) {
				constraints("spanx");
			} else if (count > 1) {
				constraints("spanx " + count);
			}
			return this;
		}

		public Builder<T> horizontalSpan() {
			return horizontal(-1);
		}

		public Builder<T> grow() {
			return constraints("grow");
		}

		public Builder<T> growx() {
			return constraints("growx");
		}

		public Builder<T> growy() {
			return constraints("growy");
		}

		public Builder<T> sizegroup(String groupName) {
			return constraints("sg " + groupName);
		}

		public Builder<T> fullWidth() {
			return constraints("width 100:300:300");
		}

		public <C> Builder<T> customize(Consumer<C> customizer) {
			this.customizer = customizer;
			return this;
		}

		@Override
		protected PropertyPanelDef b() {
			return new PropertyPanelDef(adapter, handler, tableColumns, name, title, tooltip, valueList, validation, validators, hideIf,
					constraints, customizer);
		}

		private static final String join(String... constraints) {
			return Joiner.on(", ").skipNulls().join(constraints);
		}
	}
}
