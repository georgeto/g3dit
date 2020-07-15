package de.george.g3dit.rpc.proto;

import com.google.protobuf.ByteString;

import de.george.g3dit.rpc.proto.G3RemoteControlProtos.EulerAngles;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.Position;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.PropertyIdentifier;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.PropertySerialized;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.Vector;
import de.george.g3utils.io.G3FileWriterVirtual;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.properties.ClassProperty;
import de.george.lrentnode.util.ClassUtil;

public abstract class DTC {
	public static final bCVector convert(Vector from) {
		return new bCVector(from.getX(), from.getY(), from.getZ());
	}

	public static final G3RemoteControlProtos.Vector convert(bCVector from) {
		return Vector.newBuilder().setX(from.getX()).setY(from.getY()).setZ(from.getZ()).build();
	}

	public static final bCEulerAngles convert(G3RemoteControlProtos.EulerAngles from) {
		return bCEulerAngles.fromRadian(from.getYaw(), from.getPitch(), from.getRoll());
	}

	public static final EulerAngles convert(bCEulerAngles from) {
		return EulerAngles.newBuilder().setYaw(from.getYawRad()).setPitch(from.getPitchRad()).setRoll(from.getRollRad()).build();
	}

	public static final Position convert(bCEulerAngles rotation, bCVector scaling, bCVector translation) {
		return Position.newBuilder().setRotation(convert(rotation)).setScale(convert(scaling)).setTranslation(convert(translation))
				.build();
	}

	public static final PropertyIdentifier convert(String propertySet, String propertyName) {
		return PropertyIdentifier.newBuilder().setPropertySet(propertySet).setProperty(propertyName).build();
	}

	public static final <T extends G3Serializable> PropertySerialized convert(PropertyDescriptor<T> desc, T property) {
		return convert(desc.getPropertySetName(), new ClassProperty<>(desc.getName(), desc.getDataTypeName(), property));
	}

	public static final PropertySerialized convert(String propertySet, ClassProperty<?> property) {
		G3FileWriterVirtual writer = new G3FileWriterVirtual();
		property.write(writer);
		return PropertySerialized.newBuilder().setIdentifier(DTC.convert(propertySet, property.getName()))
				.setData(ByteString.copyFrom(writer.getData())).build();
	}

	public static final PropertyIdentifier convertPropertySet(String propertySet) {
		return PropertyIdentifier.newBuilder().setPropertySet(propertySet).build();
	}

	public static final PropertySerialized convert(G3Class propertySet) {
		G3FileWriterVirtual writer = new G3FileWriterVirtual();
		ClassUtil.writeSubClass(writer, propertySet);
		return PropertySerialized.newBuilder().setIdentifier(DTC.convertPropertySet(propertySet.getClassName()))
				.setData(ByteString.copyFrom(writer.getData())).build();
	}
}
