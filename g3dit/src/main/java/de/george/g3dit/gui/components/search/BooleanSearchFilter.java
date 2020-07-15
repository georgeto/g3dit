package de.george.g3dit.gui.components.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BooleanSearchFilter<T> implements SearchFilter<T> {
	private BooleanOperator operator;
	private boolean negate;
	private List<SearchFilter<T>> filters;

	public BooleanSearchFilter(BooleanOperator operator, boolean negate) {
		this.operator = operator;
		this.negate = negate;
		this.filters = new ArrayList<>();
	}

	public BooleanOperator getOperator() {
		return operator;
	}

	public void setOperator(BooleanOperator operator) {
		this.operator = operator;
	}

	public boolean isNegate() {
		return negate;
	}

	public void setNegate(boolean negate) {
		this.negate = negate;
	}

	public List<SearchFilter<T>> getFilters() {
		return Collections.unmodifiableList(filters);
	}

	public boolean addFilter(SearchFilter<T> filter) {
		if (filters.contains(filter)) {
			return false;
		}

		filters.add(filter);
		return true;
	}

	public boolean removeFilter(SearchFilter<T> filter) {
		return filters.remove(filter);
	}

	@Override
	public boolean matches(T value) {
		boolean matches = false;
		for (SearchFilter<T> filter : filters) {
			if (filter.matches(value)) {
				matches = true;
				if (operator == BooleanOperator.Or) {
					break;
				}
			} else if (operator == BooleanOperator.And) {
				matches = false;
				break;
			}
		}

		return negate ? !matches : matches;
	}

	@Override
	public boolean isValid() {
		return filters.size() >= 1 && filters.stream().allMatch(SearchFilter::isValid);
	}

}
