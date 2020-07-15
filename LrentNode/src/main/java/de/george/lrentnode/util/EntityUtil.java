package de.george.lrentnode.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Pair;
import de.george.g3utils.util.ReflectionUtils;
import de.george.lrentnode.archive.ArchiveEntity;
import de.george.lrentnode.archive.ArchiveFile.ArchiveType;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.archive.lrentdat.LrentdatEntity;
import de.george.lrentnode.archive.node.NodeEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.eCVisualAnimation_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.eEStaticLighingType;
import de.george.lrentnode.properties.eCEntityProxy;
import de.george.lrentnode.util.ClassUtil.G3ClassHolder;

public class EntityUtil {
	/**
	 * Dupliziert die Entity und vergibt eine zufällige Guid
	 *
	 * @param sEntity
	 * @return
	 */
	public static eCEntity cloneEntity(eCEntity sEntity) {
		eCEntity entity = sEntity.clone();
		entity.setGuid(GuidUtil.randomGUID());
		return entity;
	}

	/**
	 * Überprüft ob die Entity in der Klasse gCInteraction_PS einen Owner eingetragen hat
	 *
	 * @param entity Entity
	 * @return <code>true</code> falls <code>entity</code> einen Owner hat
	 */
	public static boolean hasOwner(eCEntity entity) {
		return getOwner(entity) != null;
	}

	/**
	 * Gibt die Guid des Owners von <code>entity</code>
	 *
	 * @param entity Entity
	 * @return Guid des Owners von <code>entity</code>, oder <code>null</code> falls keiner
	 *         eingetragen ist
	 */
	public static String getOwner(eCEntity entity) {
		if (entity.hasClass(CD.gCInteraction_PS.class)) {
			return entity.getClass(CD.gCInteraction_PS.class).property(CD.gCInteraction_PS.Owner).getGuid();
		}
		return null;
	}

	/**
	 * Überprüft ob die als Owner eingetragene Entity auch als Master in der SubEntityDefintion
	 * eingetragen ist
	 *
	 * @param file ArchiveFile das die Entity enthält
	 * @param entity Zu überprüfende Child/Slave-Entity
	 * @return Wenn Entity keinen Owner hat - 0<br>
	 *         Wenn Owner ungleich Parent - 1<br>
	 *         Wenn Owner gleich Parent - 2
	 */
	public static int checkOwnerMapping(eCEntity entity) {
		if (entity.hasClass(CD.gCInteraction_PS.class)) {
			eCEntityProxy proxy = entity.getClass(CD.gCInteraction_PS.class).property(CD.gCInteraction_PS.Owner);
			if (proxy.getGuid() == null) {
				return 0;
			}
			if (!entity.hasParent()) {
				return 1;
			}
			if (proxy.getGuid().equals(entity.getParent().getGuid())) {
				return 2;
			} else {
				return 1;
			}
		}

		return 0;
	}

	/**
	 * Ermittelt statische Mesh-Klasse von <code>entity</code> (eCVisualMeshStatic_PS od.
	 * eCVisualMeshDynamic_PS)
	 *
	 * @param entity
	 * @return null, wenn die <code>entity</code> kein Mesh hat
	 */
	public static G3Class getStaticMeshClass(G3ClassContainer entity) {
		G3Class clazz = entity.getClass(CD.eCVisualMeshStatic_PS.class);
		if (clazz == null) {
			clazz = entity.getClass(CD.eCVisualMeshDynamic_PS.class);
		}
		return clazz;
	}

	/**
	 * Ermittelt animierte Mesh-Klasse von <code>entity</code> (eCVisualAnimation_PS)
	 *
	 * @param entity
	 * @return null, wenn die <code>entity</code> kein Mesh hat
	 */
	public static eCVisualAnimation_PS getAnimatedMeshClass(G3ClassContainer entity) {
		return entity.getClass(CD.eCVisualAnimation_PS.class);
	}

	/**
	 * Ermittelt Meshname (*.fxa oder *.xcmsh) von <code>entity</code>
	 *
	 * @param entity
	 * @return null, wenn die <code>entity</code> kein Mesh hat
	 */
	public static Optional<String> getMesh(G3ClassContainer entity) {
		return getMeshAndMaterialSwitch(entity).map(Pair::el0);
	}

	/**
	 * Ermittelt Meshname (*.fxa oder *.xcmsh) und MaterialSwitch von <code>entity</code>
	 *
	 * @param entity
	 * @return <code>{Mesh, MaterialSwitch}</code> oder <code>null</code> wenn <code>entity</code>
	 *         kein Mesh hat
	 */
	public static Optional<Pair<String, Integer>> getMeshAndMaterialSwitch(G3ClassContainer entity) {
		String mesh = null;
		int mSwitch = -1;
		G3Class clazz = getStaticMeshClass(entity);
		if (clazz != null) {
			mesh = clazz.property(CD.eCVisualMeshBase_PS.ResourceFileName).getString();
		} else {
			clazz = getAnimatedMeshClass(entity);
			if (clazz != null) {
				mesh = clazz.property(CD.eCVisualAnimation_PS.ResourceFilePath).getString();
			}

		}
		if (mesh != null) {
			mSwitch = clazz.property(CD.eCVisualMeshBase_PS.MaterialSwitch).getInt();
			return Optional.of(new Pair<>(mesh, mSwitch));
		}
		return Optional.empty();
	}

	public static String cleanAnimatedMeshName(String mesh) {
		return mesh != null ? mesh.replace(".FXA", ".fxa").replace(".fxa", ".xact") : mesh;
	}

	/**
	 * Ermittelt SpeedTreeMesh (*.spt) von <code>entity</code>
	 *
	 * @param entity
	 * @return null, wenn die <code>entity</code> kein Mesh hat
	 */
	public static Optional<String> getTreeMesh(G3ClassContainer entity) {
		G3Class clazz = entity.getClass(CD.eCSpeedTree_PS.class);
		if (clazz != null) {
			return Optional.ofNullable(clazz.property(CD.eCSpeedTree_PS.ResourceFilePath).getString());
		}
		return Optional.empty();
	}

	public static Optional<String> toLowPolyMesh(String meshName) {
		if (meshName.endsWith(".xcmsh") || meshName.endsWith(".xlmsh")) {
			return Optional.of(meshName.replaceFirst("\\.x[cl]msh", "_lowpoly.xcmsh"));
		} else {
			return Optional.empty();
		}
	}

	/**
	 * <code>entity</code>->gCInteraction_PS->UseType
	 *
	 * @param entity
	 * @return -1 wenn entity die Klasse gCInteraction_PS nicht besitzt
	 */
	public static int getUseType(G3ClassContainer entity) {
		if (entity.hasClass(CD.gCInteraction_PS.class)) {
			return entity.getClass(CD.gCInteraction_PS.class).property(CD.gCInteraction_PS.UseType).getEnumValue();
		}
		return -1;
	}

	public static NodeEntity convertToNodeEntity(ArchiveEntity entity) {
		NodeEntity nodeEntity = new NodeEntity(false);
		copyEntityFields(entity, nodeEntity);
		nodeEntity.setCreator(null);
		nodeEntity.setFile(null);
		nodeEntity.setParent(null);
		nodeEntity.removeAllChildren(false);
		nodeEntity.updateLocalNodeBoundary(nodeEntity.getLocalNodeBoundary());
		return nodeEntity;
	}

	public static LrentdatEntity convertToLrentdatEntity(ArchiveEntity entity) {
		LrentdatEntity lrentdatEntity = new LrentdatEntity(false);
		copyEntityFields(entity, lrentdatEntity);
		lrentdatEntity.setFile(null);
		lrentdatEntity.setParent(null);
		lrentdatEntity.removeAllChildren(false);
		return lrentdatEntity;
	}

	private static void copyEntityFields(ArchiveEntity source, ArchiveEntity destination) {
		for (Field field : ReflectionUtils.getAllFields(ArchiveEntity.class, f -> !Modifier.isStatic(f.getModifiers()))) {
			ReflectionUtils.setAccessible(field);
			ReflectionUtils.setFieldValue(field, destination, ReflectionUtils.getFieldValue(field, source));
		}
	}

	public static Optional<bCVector> getStaticNodeCoordinates(String name) {
		if (Strings.isNullOrEmpty(name)) {
			return Optional.empty();
		}

		Matcher matcher = Pattern.compile("G3_World_01_x(-?\\d+)y0z(-?\\d+)_CStat").matcher(name);
		if (matcher.find() && matcher.groupCount() == 2) {
			int x = Integer.valueOf(matcher.group(1));
			int z = Integer.valueOf(matcher.group(2));

			return Optional.of(new bCVector(x, 0, z));
		}

		return Optional.empty();
	}

	public static boolean isOutsideOfStaticNodesArea(bCVector entityPosition, bCVector nc) {
		return !(entityPosition.getX() >= nc.getX() - 5000 && entityPosition.getX() <= nc.getX() + 5000
				&& entityPosition.getZ() >= nc.getZ() - 5000 && entityPosition.getZ() <= nc.getZ() + 5000);
	}

	public static boolean isOutsideOfStaticNodesArea(bCVector entityPosition, String staticNodeName) {
		return getStaticNodeCoordinates(staticNodeName).map(nc -> isOutsideOfStaticNodesArea(entityPosition, nc)).orElse(false);
	}

	/**
	 * Entity ist mit großer Wahrscheinlichkeit eine Root-Entity, wenn sie keine PropertySets, 0/0/0
	 * als WorldPosition und eine ungültige WorldNodeBoundary hat.
	 */
	public static boolean isRootLike(eCEntity entity) {
		return entity.getClassCount() == 0 && entity.getWorldPosition().equals(bCVector.nullVector())
				&& !entity.getWorldNodeBoundary().isValid();
	}

	public static eCEntity newRootEntity(ArchiveType type) {
		eCEntity root;
		if (type == ArchiveType.Lrentdat) {
			root = new LrentdatEntity(true);
		} else {
			root = new NodeEntity(true);
		}
		root.setName("RootEntity");
		return root;
	}

	public static eCEntity newLowPolyEntity(String meshName, float objectCullFactor) {
		NodeEntity entity = new NodeEntity(true);
		entity.setRangedObjectCulling(true);
		entity.setObjectCullFactor(objectCullFactor);

		entity.addClass(new G3ClassHolder(ClassUtil.createDefaultPropertySet(CD.eCVisualMeshStatic_PS.class)));
		entity.addClass(new G3ClassHolder(ClassUtil.createDefaultPropertySet(CD.eCIlluminated_PS.class)));

		entity.getProperty(CD.eCVisualMeshBase_PS.ResourceFileName).setString(meshName);
		entity.getProperty(CD.eCVisualMeshBase_PS.StaticLightingType).setEnumValue(eEStaticLighingType.eEStaticLighingType_Instance);
		entity.getProperty(CD.eCIlluminated_PS.CastShadows).setBool(false);
		entity.getProperty(CD.eCIlluminated_PS.ReciveShadows).setBool(false);
		entity.getProperty(CD.eCIlluminated_PS.ReciveTreeShadows).setBool(false);
		entity.getProperty(CD.eCIlluminated_PS.CastStaticShadows).setBool(false);
		entity.getProperty(CD.eCIlluminated_PS.ReciveStaticShadows).setBool(false);

		return entity;
	}
}
