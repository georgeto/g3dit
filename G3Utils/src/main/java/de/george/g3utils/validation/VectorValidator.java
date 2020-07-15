package de.george.g3utils.validation;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;

import de.george.g3utils.structure.bCVector;

public class VectorValidator extends AbstractValidator<String> {
	public static final VectorValidator INSTANCE = new VectorValidator();

	private VectorValidator() {
		super(String.class);
	}

	@Override
	public void validate(Problems problems, String compName, String model) {

		try {
			bCVector.fromString(model);
		} catch (IllegalArgumentException e) {
			problems.append(compName + " enthält keinen gültige Vektor.");
		}
	}
}
