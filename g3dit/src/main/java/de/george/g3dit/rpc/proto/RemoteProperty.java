package de.george.g3dit.rpc.proto;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.Iterables;

import de.george.g3dit.rpc.IpcHelper;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.PropertyIdentifier;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.PropertyRequest;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.PropertyResponse;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.PropertySerialized;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.ResponseContainer;
import de.george.g3dit.rpc.zmq.ResponseCallback;
import de.george.g3dit.rpc.zmq.ResponseCallback.Status;
import de.george.g3utils.io.G3FileReaderVirtual;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.util.Pair;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.archive.lrentdat.LrentdatEntity;
import de.george.lrentnode.archive.node.NodeEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.ClassDescriptor;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.properties.ClassProperty;
import de.george.lrentnode.template.TemplateEntity;
import de.george.lrentnode.util.ClassUtil;

public abstract class RemoteProperty {
	public static class RemotePropertyException extends IOException {
		public RemotePropertyException(String message) {
			super(message);
		}
	}

	public static final <T extends G3Serializable> CompletableFuture<ClassProperty<T>> getProperty(String entityGuid,
			PropertyDescriptor<T> property) {
		return getProperty(entityGuid, property.getPropertySetName(), property.getName());
	}

	public static final <T extends G3Serializable> CompletableFuture<ClassProperty<T>> getProperty(String entityGuid, String propertySet,
			String propertyName) {
		return getPropertyIdentifier(entityGuid, DTC.convert(propertySet, propertyName));
	}

	public static final <T extends G3Serializable> CompletableFuture<ClassProperty<T>> setProperty(String entityGuid,
			PropertyDescriptor<T> property, T value) {
		return setPropertySerialized(entityGuid, DTC.convert(property, value));
	}

	public static final <T extends G3Serializable> CompletableFuture<ClassProperty<T>> setProperty(String entityGuid, String propertySet,
			ClassProperty<T> property) {
		return setPropertySerialized(entityGuid, DTC.convert(propertySet, property));
	}

	public static final <T extends G3Class> CompletableFuture<T> getPropertySet(String entityGuid, String propertySet) {
		return getPropertyIdentifier(entityGuid, DTC.convertPropertySet(propertySet));
	}

	public static final <T extends G3Class> CompletableFuture<T> getPropertySet(String entityGuid,
			Class<? extends ClassDescriptor> propertySet) {
		return getPropertySet(entityGuid, ClassDescriptor.getName(propertySet));
	}

	public static final <T extends G3Class> CompletableFuture<T> setPropertySet(String entityGuid, G3Class propertySet) {
		return setPropertySerialized(entityGuid, DTC.convert(propertySet));
	}

	public static final <T extends eCEntity> CompletableFuture<T> getEntity(String entityGuid) {
		return getPropertyIdentifier(entityGuid, PropertyIdentifier.newBuilder().build());
	}

	private enum PropertyType {
		Entity,
		PropertySet,
		Property;

		public static PropertyType fromIdentifier(PropertyIdentifier identifier) {
			if (!identifier.hasPropertySet()) {
				return PropertyType.Entity;
			} else if (!identifier.hasProperty()) {
				return PropertyType.PropertySet;
			} else {
				return Property;
			}
		}
	}

	private static final <T> CompletableFuture<T> getPropertyIdentifier(String entityGuid, PropertyIdentifier property) {
		PropertyRequest.Builder requestBuilder = PropertyRequest.newBuilder()
				.setEntityGuid(GuidUtil.hexToGuidText(GuidUtil.parseGuid(entityGuid)));
		requestBuilder.addPropertiesGet(property);

		return sendPropertyRequest(requestBuilder.build(), PropertyType.fromIdentifier(property));
	}

	private static final <T> CompletableFuture<T> setPropertySerialized(String entityGuid, PropertySerialized property) {
		PropertyRequest.Builder requestBuilder = PropertyRequest.newBuilder()
				.setEntityGuid(GuidUtil.hexToGuidText(GuidUtil.parseGuid(entityGuid)));
		requestBuilder.addPropertiesSet(property);

		return sendPropertyRequest(requestBuilder.build(), PropertyType.fromIdentifier(property.getIdentifier()));
	}

	private static final <T> CompletableFuture<T> sendPropertyRequest(PropertyRequest request, PropertyType propertyType) {
		CompletableFuture<T> result = new CompletableFuture<>();
		IpcHelper.getIpc().sendRequest(request, RemoteProperty::handleReceiveProperty, Pair.of(propertyType, result));
		return result;
	}

	private static final void handleReceiveProperty(Status s, ResponseContainer rc, Object ud) {
		Pair<PropertyType, CompletableFuture<Object>> userData = (Pair<PropertyType, CompletableFuture<Object>>) ud;
		PropertyType expectedType = userData.el0();
		CompletableFuture<Object> result = userData.el1();
		if (s == ResponseCallback.Status.Timeout) {
			result.completeExceptionally(new RemotePropertyException("Gothic 3 is not reachable"));
		} else if (rc.getStatus() == ResponseContainer.Status.FAILED) {
			result.completeExceptionally(new RemotePropertyException("Operation failed: " + rc.getMessage()));
		} else {
			PropertyResponse response = rc.getPropertyResponse();
			for (PropertySerialized serialized : Iterables.concat(response.getPropertiesGetList(), response.getPropertiesSetList())) {
				PropertyIdentifier identifier = serialized.getIdentifier();

				if (!serialized.hasData()) {
					result.completeExceptionally(new RemotePropertyException("Failed to find property: " + identifier));
					return;
				}

				PropertyType receivedType = PropertyType.fromIdentifier(identifier);
				if (receivedType != expectedType) {
					result.completeExceptionally(new RemotePropertyException(
							String.format("Received %s as result instead of %s.", receivedType, expectedType)));
					return;
				}

				try (G3FileReaderVirtual reader = new G3FileReaderVirtual(serialized.getData().asReadOnlyByteBuffer())) {
					switch (receivedType) {
						case Property:
							ClassProperty<G3Serializable> resultProperty = new ClassProperty<>(reader);
							result.complete(resultProperty);
							break;

						case PropertySet:
							G3Class propertySet = ClassUtil.readSubClass(reader);
							result.complete(propertySet);

						case Entity:
							// Entity
							eCEntity entity;
							String entityType = reader.readEntry();
							switch (entityType) {
								case "Dynamic" -> entity = new LrentdatEntity(false);
								case "Spatial" -> entity = new NodeEntity(false);
								case "Template" -> entity = new TemplateEntity(false);
								default -> {
									result.completeExceptionally(
											new RemotePropertyException("Received entity with unknown type: " + entityType));
									return;
								}
							}
							entity.read(reader, false);
							result.complete(entity);
							break;
					}
					return;
				} catch (Throwable e) {
					result.completeExceptionally(e);
				}
			}
		}
	}
}
