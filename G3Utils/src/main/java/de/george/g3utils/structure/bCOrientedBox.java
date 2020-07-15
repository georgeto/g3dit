package de.george.g3utils.structure;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class bCOrientedBox implements G3Serializable {
	private bCVector center, extent;
	private bCMatrix3 orientation;

	public bCOrientedBox() {
		this(bCVector.nullVector(), bCVector.negInfinity(), bCMatrix3.getIdentity());
	}

	public bCOrientedBox(bCVector center, bCVector extent, bCMatrix3 orientation) {
		this.center = center;
		this.extent = extent;
		this.orientation = orientation;
	}

	public bCOrientedBox(bCBox box, bCMatrix3 orientation) {
		center = box.getMin().getTranslated(box.getMax()).scale(0.5f).transform(orientation);
		extent = box.getMax().getInvTranslated(box.getMin()).scale(0.5f);
		this.orientation = orientation;
	}

	public bCVector getCenter() {
		return center;
	}

	public void setCenter(bCVector center) {
		this.center = center;
	}

	public bCVector getExtent() {
		return extent;
	}

	public void setExtent(bCVector extent) {
		this.extent = extent;
	}

	public bCMatrix3 getOrientation() {
		return orientation;
	}

	public void setOrientation(bCMatrix3 orientation) {
		this.orientation = orientation;
	}

	public bCVector[] getVertices() {
		bCVector xAxis = orientation.getXAxis().getScaled(extent.getX());
		bCVector yAxis = orientation.getYAxis().getScaled(extent.getY());
		bCVector zAxis = orientation.getZAxis().getScaled(extent.getZ());

		bCVector[] vertices = new bCVector[8];
		vertices[0] = center.getTranslated(xAxis).translate(yAxis).translate(zAxis);
		vertices[1] = center.getTranslated(xAxis).invTranslate(yAxis).translate(zAxis);
		vertices[2] = center.getInvTranslated(xAxis).invTranslate(yAxis).translate(zAxis);
		vertices[3] = center.getInvTranslated(xAxis).translate(yAxis).translate(zAxis);
		vertices[4] = center.getTranslated(xAxis).translate(yAxis).invTranslate(zAxis);
		vertices[5] = center.getTranslated(xAxis).invTranslate(yAxis).invTranslate(zAxis);
		vertices[6] = center.getInvTranslated(xAxis).invTranslate(yAxis).invTranslate(zAxis);
		vertices[7] = center.getInvTranslated(xAxis).translate(yAxis).invTranslate(zAxis);
		return vertices;
	}

	public bCBox getCircumAxisBox() {
		bCVector[] vertices = getVertices();
		return new bCBox(bCVector.minimum(vertices), bCVector.maximum(vertices));
	}

	public void transform(bCMatrix mat) {
		extent.scale(mat.getPureScaling());

		bCMatrix centerMat = mat.clone();
		centerMat.modifyScaling(1f);
		center.transform(centerMat);

		orientation.multiply(mat.getPureRotation().getRotation());
	}

	public bCOrientedBox getTransformed(bCMatrix mat) {
		bCOrientedBox result = clone();
		result.transform(mat);
		return result;
	}

	public void invalidate() {
		extent = bCVector.negInfinity();
	}

	public boolean isValid() {
		return !extent.isEqual(bCVector.negInfinity());
	}

	@Override
	public bCOrientedBox clone() {
		return new bCOrientedBox(center.clone(), extent.clone(), orientation.clone());
	}

	@Override
	public void read(G3FileReader reader) {
		center = reader.readVector();
		extent = reader.readVector();
		orientation = reader.read(bCMatrix3.class);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.write(center, extent, orientation);
	}

	@Override
	public String toString() {
		return "center=" + center + ", extent=" + extent + ",\norientation=\n" + orientation;
	}
}
