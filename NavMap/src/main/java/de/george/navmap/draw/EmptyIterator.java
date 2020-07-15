/*
 * GeoTools - The Open Source Java GIS Tookit http://geotools.org
 *
 * (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; version 2.1 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package de.george.navmap.draw;

/**
 * An iterator for empty geometries
 *
 * @since 2.4
 */
public class EmptyIterator extends AbstractLiteIterator {
	@Override
	public int getWindingRule() {
		return WIND_NON_ZERO;
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public void next() {
		throw new IllegalStateException();
	}

	@Override
	public int currentSegment(double[] coords) {
		return 0;
	}
}
