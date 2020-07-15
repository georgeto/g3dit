package de.george.g3dit.gui.validation;

import java.util.List;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;

import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;

public class PointDistanceValidator extends AbstractValidator<String> {
	public PointDistanceValidator() {
		super(String.class);
	}

	@Override
	public void validate(Problems problems, String compName, String model) {
		try {
			List<bCVector> points = Misc.parseVectorList(model);
			if (points.size() <= 1) {
				return;
			}

			for (int s = points.size() - 1, e = 0; e < points.size(); s = e++) {
				if (points.get(s).getInvTranslated(points.get(e)).getSquareMagnitude() < 3 * 3) {
					problems.append(
							String.format(compName + " enthält zwei sehr nah beieinander liegende Sticks: %d und %d", s + 1, e + 1),
							Severity.WARNING);
				}
			}
		} catch (IllegalArgumentException e) {
			problems.append(compName + " enthält fehlerhafte Koordinaten.", Severity.FATAL);
		}
	}

}
