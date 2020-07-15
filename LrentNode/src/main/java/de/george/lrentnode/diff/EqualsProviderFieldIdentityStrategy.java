/*
 * Copyright 2014 Daniel Bechler
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package de.george.lrentnode.diff;

import static de.danielbechler.util.Objects.isEqual;

import java.lang.reflect.Field;

import de.danielbechler.diff.identity.IdentifierIdentityStrategy;
import de.danielbechler.util.Assert;
import de.danielbechler.util.Exceptions;

/**
 * @author Daniel Bechler
 */
public class EqualsProviderFieldIdentityStrategy implements IdentifierIdentityStrategy {
	private final String equalsValueProviderField;

	public EqualsProviderFieldIdentityStrategy(final String equalsValueProviderField) {
		Assert.hasText(equalsValueProviderField, "equalsValueProviderField");
		this.equalsValueProviderField = equalsValueProviderField;
	}

	@Override
	public boolean equals(Object working, Object base) {
		final Object workingValue = access(working, equalsValueProviderField);
		final Object baseValue = access(base, equalsValueProviderField);
		return isEqual(workingValue, baseValue);
	}

	private static Object access(final Object target, final String fieldName) {
		if (target == null) {
			return null;
		}
		try {
			final Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(target);
		} catch (final IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw Exceptions.escalate(e);
		}
	}

	@Override
	public String getIdentifier(Object object) {
		return String.valueOf(access(object, equalsValueProviderField));
	}
}
