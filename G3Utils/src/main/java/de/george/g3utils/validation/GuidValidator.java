package de.george.g3utils.validation;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;

import com.teamunify.i18n.I;

import de.george.g3utils.structure.GuidUtil;

public class GuidValidator extends AbstractValidator<String> {
	public static final GuidValidator INSTANCE = new GuidValidator(false);
	public static final GuidValidator INSTANCE_ALLOW_EMPTY = new GuidValidator(true);

	boolean emptyAllowed;

	private GuidValidator(boolean emptyAllowed) {
		super(String.class);
		this.emptyAllowed = emptyAllowed;
	}

	@Override
	public void validate(Problems problems, String compName, String model) {

		if (emptyAllowed && model.isEmpty()) {
			return;
		}
		if (GuidUtil.parseGuid(model) != null) {
			return;
		}

		problems.append(I.trf("{0} does not contain a valid Guid.", compName));
	}

}
