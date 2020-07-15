package de.george.g3utils.validation;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;

public class EmtpyWarnValidator extends AbstractValidator<String> {
	public static final EmtpyWarnValidator INSTANCE = new EmtpyWarnValidator();

	private EmtpyWarnValidator() {
		super(String.class);
	}

	@Override
	public void validate(Problems problems, String compName, String model) {
		if (model.isEmpty()) {
			problems.append(compName + " sollte nicht leer sein.", Severity.WARNING);
		}

	}

}
