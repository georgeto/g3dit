package de.george.lrentnode.classes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCMatrix3;
import de.george.g3utils.structure.bCOrientedBox;
import de.george.g3utils.structure.bCSphere;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.eECollisionShapeType;
import de.george.lrentnode.enums.G3Enums.eEShapeAABBAdapt;
import de.george.lrentnode.enums.G3Enums.eEShapeGroup;
import de.george.lrentnode.enums.G3Enums.eEShapeMaterial;
import de.george.lrentnode.properties.bTPropertyContainer;
import de.george.lrentnode.properties.gBool;
import de.george.lrentnode.properties.gFloat;
import de.george.lrentnode.properties.gShort;

public class eCCollisionShape extends G3Class {
	private static final Logger logger = LoggerFactory.getLogger(eCCollisionShape.class);

	private Shape shape;

	// OuterAABBLocal - bCBox
	private bCBox outerAABBLocal;
	private bCVector lastPosition;

	/**
	 * Erstellt ein neues CollisionShape, welches ein Box Shape enthält, die ShapeGroup MeleeWeapon
	 * und das ShapeMaterial Metal hat.
	 *
	 * @param prefixed
	 */
	public eCCollisionShape(boolean prefixed) {
		super("eCCollisionShape", 0x4A);
		shape = new BoxShape(new bCOrientedBox());
		outerAABBLocal = new bCBox();
		lastPosition = bCVector.nullVector();
		addProperty(CD.eCCollisionShape.ShapeType, new bTPropertyContainer<>(eECollisionShapeType.eECollisionShapeType_Box));
		addProperty(CD.eCCollisionShape.Group, new bTPropertyContainer<>(eEShapeGroup.eEShapeGroup_MeleeWeapon));
		addProperty(CD.eCCollisionShape.Material, new bTPropertyContainer<>(eEShapeMaterial.eEShapeMaterial_Metal));
		addProperty(CD.eCCollisionShape.ShapeAABBAdaptMode, new bTPropertyContainer<>(eEShapeAABBAdapt.eEShapeAABBAdapt_None));
		addProperty(CD.eCCollisionShape.IgnoredByTraceRay, new gBool(false));
		addProperty(CD.eCCollisionShape.EnableCCD, new gBool(false));
		addProperty(CD.eCCollisionShape.OverrideEntityAABB, new gBool(false));
		addProperty(CD.eCCollisionShape.DisableCollision, new gBool(false));
		addProperty(CD.eCCollisionShape.DisableResponse, new gBool(false));
		addProperty(CD.eCCollisionShape.TriggersOnTouch, new gBool(false));
		addProperty(CD.eCCollisionShape.TriggersOnUntouch, new gBool(false));
		addProperty(CD.eCCollisionShape.TriggersOnIntersect, new gBool(false));
		addProperty(CD.eCCollisionShape.SkinWidth, new gFloat(-1));
		addProperty(CD.eCCollisionShape.IsLazyGenerated, new gBool(false));
		addProperty(CD.eCCollisionShape.FileVersion, new gShort((short) 0x4A));
	}

	public eCCollisionShape(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		int shapeType = property(CD.eCCollisionShape.ShapeType).getEnumValue();
		switch (shapeType) {
			case eECollisionShapeType.eECollisionShapeType_TriMesh:
			case eECollisionShapeType.eECollisionShapeType_ConvexHull:
				shape = reader.read(FileShape.class);
				break;
			case eECollisionShapeType.eECollisionShapeType_Box:
				shape = reader.read(BoxShape.class);
				break;
			case eECollisionShapeType.eECollisionShapeType_Capsule:
				shape = reader.read(CapsuleShape.class);
				break;
			case eECollisionShapeType.eECollisionShapeType_Sphere:
				shape = reader.read(SphereShape.class);
				break;
			case eECollisionShapeType.eECollisionShapeType_Point:
				shape = reader.read(PointShape.class);
				break;
			default:
				reader.info(logger, "Unknown ShapeType {}", shapeType);
				shape = reader.read(UnknownShape.class, deadcodePosition - reader.getPos() - 38);
				break;
		}

		// eCCollisionShape internal data
		reader.skip(2); // bCObjectRefBase Version
		outerAABBLocal = reader.readBox();
		lastPosition = reader.readVector();

		if (shape instanceof FileShape) {
			((FileShape) shape).setMeshBoundary(outerAABBLocal.clone());
		}
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		shape.write(writer);

		// eCCollisionShape internal data
		writer.writeUnsignedShort(1); // bCObjectRefBase Version
		writer.writeBox(outerAABBLocal);
		writer.writeVector(lastPosition);
	}

	public Shape getShape() {
		return shape;
	}

	public void setShape(Shape shape) {
		this.shape = shape;
		calcShapeAABBLocal();
	}

	public bCBox getOuterAABBLocal() {
		return outerAABBLocal;
	}

	public void setOuterAABBLocal(bCBox outerAABBLocal) {
		this.outerAABBLocal = outerAABBLocal;
	}

	public bCVector getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(bCVector lastPosition) {
		this.lastPosition = lastPosition;
	}

	private void calcShapeAABBLocal() {
		if (shape instanceof FileShape) {
			outerAABBLocal = ((FileShape) shape).getShapeBoundary().clone();
		} else if (shape instanceof BoxShape) {
			BoxShape box = (BoxShape) shape;
			bCOrientedBox orientedBox = box.getOrientedBox();
			bCBox circumAxisBox = orientedBox.getCircumAxisBox();
			outerAABBLocal.setMin(circumAxisBox.getMin());
			outerAABBLocal.setMax(circumAxisBox.getMax());
		} else if (shape instanceof SphereShape) {
			SphereShape sphere = (SphereShape) shape;
			outerAABBLocal.setBox(sphere.getSphere().getPosition(), sphere.getSphere().getRadius());
		} else if (shape instanceof CapsuleShape) {
			CapsuleShape capsule = (CapsuleShape) shape;
			bCVector xOffset = capsule.orientation.getXAxis().getScaled(capsule.radius);
			float height = capsule.height * 0.5f + capsule.radius;
			bCVector yOffset = capsule.orientation.getYAxis().getScaled(height);

			outerAABBLocal.setMin(capsule.center.getInvTranslated(yOffset).getInvTranslated(xOffset));
			outerAABBLocal.setMax(capsule.center.getTranslated(yOffset).getTranslated(xOffset));
		} else if (shape instanceof PointShape) {
			outerAABBLocal.setMin(new bCVector(0, 0, 0));
			outerAABBLocal.setMax(new bCVector(0, 0, 0));
		}
	}

	public static Shape getDefaultShape(int shapeType) {
		switch (shapeType) {
			case eECollisionShapeType.eECollisionShapeType_TriMesh:
			case eECollisionShapeType.eECollisionShapeType_ConvexHull:
				return new FileShape("", 0);
			case eECollisionShapeType.eECollisionShapeType_Box:
				return new BoxShape(new bCOrientedBox());
			case eECollisionShapeType.eECollisionShapeType_Capsule:
				return new CapsuleShape(0, 0, bCMatrix3.getIdentity(), bCVector.nullVector());
			case eECollisionShapeType.eECollisionShapeType_Sphere:
				return new SphereShape(new bCSphere());
			case eECollisionShapeType.eECollisionShapeType_Point:
				return new PointShape(bCVector.nullVector());
			default:
				return null;
		}
	}

	public interface Shape extends G3Serializable {
	}

	public static class FileShape implements Shape {

		private String shapeFile;
		private int resourceIndex;
		private bCBox meshBoundary = new bCBox();

		public FileShape(String shapeFile, int resourceIndex) {
			this.shapeFile = shapeFile;
			this.resourceIndex = resourceIndex;
		}

		public String getShapeFile() {
			return shapeFile;
		}

		public void setShapeFile(String shapeFile) {
			this.shapeFile = shapeFile;
		}

		public int getResourceIndex() {
			return resourceIndex;
		}

		public void setResourceIndex(int resourceIndex) {
			this.resourceIndex = resourceIndex;
		}

		public bCBox getShapeBoundary() {
			return meshBoundary;
		}

		public void setMeshBoundary(bCBox meshBoundary) {
			this.meshBoundary = meshBoundary;
		}

		@Override
		public void read(G3FileReader reader) {
			shapeFile = reader.readEntry();
			resourceIndex = reader.readShort();
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeEntry(shapeFile);
			writer.writeUnsignedShort(resourceIndex);
		}
	}

	public static class BoxShape implements Shape {

		private bCOrientedBox orientedBox;

		public BoxShape(bCOrientedBox orientedBox) {
			this.orientedBox = orientedBox;
		}

		public bCOrientedBox getOrientedBox() {
			return orientedBox;
		}

		public void setOrientedBox(bCOrientedBox orientedBox) {
			this.orientedBox = orientedBox;
		}

		@Override
		public void read(G3FileReader reader) {
			orientedBox = new bCOrientedBox(reader.readVector(), reader.readVector(), reader.read(bCMatrix3.class));
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.write(orientedBox);
		}

	}

	public static class CapsuleShape implements Shape {

		private float height, radius;
		private bCMatrix3 orientation;
		private bCVector center;

		public CapsuleShape(float height, float radius, bCMatrix3 orientation, bCVector center) {
			this.height = height;
			this.radius = radius;
			this.orientation = orientation;
			this.center = center;
		}

		public float getHeight() {
			return height;
		}

		public void setHeight(float height) {
			this.height = height;
		}

		public float getRadius() {
			return radius;
		}

		public void setRadius(float radius) {
			this.radius = radius;
		}

		public bCMatrix3 getOrientation() {
			return orientation;
		}

		public void setOrientation(bCMatrix3 orientation) {
			this.orientation = orientation;
		}

		public bCVector getCenter() {
			return center;
		}

		public void setCenter(bCVector center) {
			this.center = center;
		}

		@Override
		public void read(G3FileReader reader) {
			height = reader.readFloat();
			radius = reader.readFloat();
			orientation = reader.read(bCMatrix3.class);
			center = reader.readVector();
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeFloat(height);
			writer.writeFloat(radius);
			writer.write(orientation);
			writer.writeVector(center);
		}
	}

	public static class SphereShape implements Shape {

		private bCSphere sphere;

		public SphereShape(bCSphere sphere) {
			this.sphere = sphere;
		}

		public bCSphere getSphere() {
			return sphere;
		}

		public void setSphere(bCSphere sphere) {
			this.sphere = sphere;
		}

		@Override
		public void read(G3FileReader reader) {
			sphere = reader.read(bCSphere.class);
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.write(sphere);
		}
	}

	public static class PointShape implements Shape {

		private bCVector position;

		public PointShape(bCVector position) {
			this.position = position;

		}

		public bCVector getPosition() {
			return position;
		}

		public void setPosition(bCVector point) {
			position = point;
		}

		@Override
		public void read(G3FileReader reader) {
			position = reader.readVector();
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeVector(position);
		}

	}

	public static class UnknownShape implements Shape {

		private String raw;

		@Override
		public void read(G3FileReader reader) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void read(G3FileReader reader, int size) {
			raw = reader.read(size);
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.write(raw);
		}
	}
}
