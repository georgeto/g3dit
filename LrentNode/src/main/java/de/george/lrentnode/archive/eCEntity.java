package de.george.lrentnode.archive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.FluentIterable;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3FileWriterEx;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCSphere;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.template.TemplateEntity;
import de.george.lrentnode.util.EntityUtil;

public abstract class eCEntity extends G3ClassContainer {
	private String guid;

	// Beginning of reusable 2nd HeaderPart
	protected boolean enabled;
	protected boolean renderingEnabled;
	protected boolean processingDisabled;
	protected boolean deactivationEnabled;
	protected boolean pickable;
	protected boolean collisionEnabled;
	protected float renderAlphaValue;
	protected int insertType;
	protected int lastRenderPriority;
	protected boolean specialDepthTexPassEnabled;
	/**
	 * eSEntityFlags:__FIXME_13 @ 0x2000 | Must Compiled Static ?
	 */
	protected boolean unkFlag2;

	protected String name;
	protected bCMatrix worldMatrix;
	protected bCMatrix localMatrix;
	protected bCBox worldTreeBoundary;
	protected bCBox localNodeBoundary;
	protected bCBox worldNodeBoundary;
	protected bCSphere worldTreeSphere;
	protected bCSphere worldNodeSphere;

	protected float visualLoDFactor;
	/**
	 * eSEntityFlags:__FIXME_22 @ 0x400000 | Derived Template | DerivedFromTemplate | %s - warning:
	 * entity: '%s' hasn´t no creatortemplate found, but it seems that an associated template-entity
	 * must exist. Corrupt template-database
	 */
	protected boolean unkFlag3;
	/**
	 * Gibt in Abhängigkeit von der Objektgröße an, ab welcher Entfernung ein Objekt ausgeblendet
	 * werden soll. Je kleiner ObjectCullFactor und je größer das Objekt, desto weiter weg die
	 * Ausblendung.
	 */
	protected float objectCullFactor;
	/**
	 * unsigned long
	 */
	protected long dataChangedTimeStamp;
	protected float uniformScaling;
	/**
	 * eSEntityFlags:__FIXME_27 @ 0x8000000 | Ranged Object Culling ? | LowPoly-Entities have this
	 * bit set!
	 */
	protected boolean rangedObjectCulling;
	/**
	 * Gibt an, ob Objekt bereits dann ausgeblendet werden soll, wenn es aus der ROI (also nicht
	 * Sichtweite) verschwindet.
	 */
	protected boolean processingRangeOutFadingEnabled;

	protected eCEntity parent;
	protected List<eCEntity> childs = new ArrayList<>();

	public eCEntity(boolean initialize) {
		super();
		if (initialize) {
			guid = GuidUtil.randomGUID();
			invalidate();
		}
	}

	private void invalidate() {
		// eCNode::invalidate()
		parent = null;
		// this.currentContext = null;

		// @fon
		/*
		 * Flags Set m_Enabled m_DeactivationEnabled m_Pickable m_CollisionEnabled Kept __FIXME_4
		 * __FIXME_30 __FIXME_31
		 */
		// @foff
		enabled = true;
		renderingEnabled = false;
		processingDisabled = false;
		deactivationEnabled = true;
		pickable = true;
		collisionEnabled = true;
		renderAlphaValue = 1.0f;
		insertType = 0;
		lastRenderPriority = 0;
		specialDepthTexPassEnabled = false;
		unkFlag2 = false;

		name = "";
		worldMatrix = bCMatrix.getIdentity();
		localMatrix = bCMatrix.getIdentity();
		worldTreeBoundary = new bCBox();
		localNodeBoundary = new bCBox();
		worldNodeBoundary = new bCBox();
		worldTreeSphere = new bCSphere();
		worldNodeSphere = new bCSphere();

		visualLoDFactor = 1.0f;
		unkFlag3 = false;
		objectCullFactor = 1.0f;
		dataChangedTimeStamp = -1;
		uniformScaling = 1.0f;
		rangedObjectCulling = false;
		processingRangeOutFadingEnabled = false;
		invalidateFlags();
	}

	/**
	 * Für Flags die im Bitfield EntityFlags gespeichert sind, aber nur von spezialisierter Entity serialisiert werden
	 */
	protected void invalidateFlags() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public bCMatrix getWorldMatrix() {
		return worldMatrix.clone();
	}

	public bCMatrix getLocalMatrix() {
		return localMatrix.clone();
	}

	public bCVector getWorldPosition() {
		return worldMatrix.getTranslation();
	}

	public bCEulerAngles getWorldRotation() {
		return new bCEulerAngles(worldMatrix);
	}

	public bCVector getLocalPosition() {
		return localMatrix.getTranslation();
	}

	public bCEulerAngles getLocalRotation() {
		return new bCEulerAngles(localMatrix);
	}

	public bCBox getWorldTreeBoundary() {
		return worldTreeBoundary.clone();
	}

	public bCBox getLocalNodeBoundary() {
		return localNodeBoundary.clone();
	}

	public bCBox getWorldNodeBoundary() {
		return worldNodeBoundary.clone();
	}

	public bCSphere getWorldTreeSphere() {
		return worldTreeSphere.clone();
	}

	public bCSphere getWorldNodeSphere() {
		return worldNodeSphere.clone();
	}

	public void setWorldPosition(bCVector newPosition) {
		// TODO: Parent checks?
		bCVector positionDelta = getLocalPosition().getInvTranslated(getWorldPosition());
		setLocalPosition(positionDelta.translate(newPosition));
	}

	public void setLocalPosition(bCVector newPosition) {
		bCMatrix mat = localMatrix.clone();
		mat.modifyTranslation(newPosition);
		setLocalMatrix(mat);
	}

	public void setLocalRotation(bCEulerAngles newRotation) {
		bCMatrix mat = localMatrix.clone();
		mat.modifyRotation(newRotation);
		setLocalMatrix(mat);
	}

	public void setLocalMatrix(bCMatrix newMatrix) {
		localMatrix = newMatrix.clone();
		updateGeometry();
	}

	public void setToWorldMatrix(bCMatrix newMatrix) {
		eCEntity parent = getParent();
		if (parent != null) {
			bCMatrix newWorldMatrix = parent.getWorldMatrix().getInverted();
			newWorldMatrix.multiply(newMatrix);
			setLocalMatrix(newWorldMatrix);
		} else {
			setLocalMatrix(newMatrix);
		}
	}

	public void updateLocalNodeBoundary(bCBox newBoundary) {
		// Compute the boundary from propertySets.GetBoundary(), but we can'eCEntity do that...
		setLocalNodeBoundary(newBoundary);
		updateGeometry();
	}

	private void setLocalNodeBoundary(bCBox newBoundary) {
		localNodeBoundary = newBoundary.clone();
	}

	private void setWorldMatrix(bCMatrix newMatrix) {
		worldMatrix = newMatrix.clone();
		onUpdatedWorldMatrix();
	}

	private void setWorldTreeBoundary(bCBox newBoundary) {
		worldTreeBoundary = newBoundary.clone();
		if (worldTreeBoundary.isValid()) {
			worldTreeSphere.setPosition(worldTreeBoundary.getCenter());
			worldTreeSphere.setRadius(worldTreeBoundary.getExtent().length());
		} else {
			worldTreeSphere.invalidate();
		}
	}

	private void setWorldNodeBoundary(bCBox newBoundary) {
		worldNodeBoundary = newBoundary.clone();
		if (worldNodeBoundary.isValid()) {
			worldNodeSphere.setPosition(worldNodeBoundary.getCenter());
			worldNodeSphere.setRadius(worldNodeBoundary.getExtent().length());
		} else {
			worldNodeSphere.invalidate();
		}
	}

	protected void onUpdatedWorldMatrix() {
		uniformScaling = worldMatrix.getPureScaling().getX();
	}

	public void updateGeometry() {
		updateChildDependencies();
		if (getParent() != null) {
			updateParentDependencies();
		}
	}

	/**
	 * <ol>
	 * <li>Berechnung der neuen WorldMatrix aus der aktuellen LocalMatrix (und der WorldMatrix des Parents)</li>
	 * <li>Berechnung der WorldNodeBoundary aus der WorldMatrix und der LocalNodeBoundary</li>
	 * <li>updateChildDependencies() für alle Childs</li>
	 * <li>Berechnung der WorldTreeBoundary aus der eigenen WorldNodeBoundary und der WorldTreeBoundary der Childs (falls sich WorldMatrix oder WorldNodeBoundary geändert hat, denn das ist Voraussetzung einer Änderung der WorldTreeBoundary)</li>
	 * </ol>
	 */
	public void updateChildDependencies() {
		bCMatrix tmpWorldMatrix;
		if (getParent() == null) {
			tmpWorldMatrix = localMatrix;
		} else {
			// Update WorldMatrix
			tmpWorldMatrix = getParent().getWorldMatrix().getProduct(localMatrix);
		}

		// Update WorldMatrix
		boolean worldMatrixChanged = !tmpWorldMatrix.isEqual(worldMatrix);
		if (worldMatrixChanged) {
			setWorldMatrix(tmpWorldMatrix);
		}

		// Update WorldNodeBoundary
		bCBox tmpWorldNodeBounadry = localNodeBoundary.clone();
		if (tmpWorldNodeBounadry.isValid()) {
			tmpWorldNodeBounadry.transform(tmpWorldMatrix);
		}
		boolean worldNodeBoundaryChanged = !tmpWorldNodeBounadry.isEqual(worldNodeBoundary);
		if (worldNodeBoundaryChanged) {
			setWorldNodeBoundary(tmpWorldNodeBounadry);
		}

		// Leave WorldTreeBoundary untouched
		if (!worldMatrixChanged && !worldNodeBoundaryChanged) {
			return;
		}

		// Initialize WorldTreeBoundary
		bCBox tmpWorldTreeBoundary = tmpWorldNodeBounadry.clone();

		// Update WorldTreeBoundary
		for (eCEntity child : getChilds()) {
			child.updateChildDependencies();
			tmpWorldTreeBoundary.merge(child.worldTreeBoundary);
		}
		setWorldTreeBoundary(tmpWorldTreeBoundary);
		// Originale Gothic 3 Implementierung
		// @foff
		/*
		bCBox tmpWorldTreeBoundary = new bCBox();
		if(getParent() == null) {
			setWorldMatrix(localMatrix);
		} else {
			// Update WorldMatrix
			bCMatrix tmpWorldMatrix = getParent().getWorldMatrix().getProduct(localMatrix);
			boolean worldMatrixChanged = !tmpWorldMatrix.isEqual(worldMatrix);
			if(worldMatrixChanged)
				setWorldMatrix(tmpWorldMatrix);

			// Update WorldNodeBoundary
			bCBox tmpWorldNodeBounadry = localNodeBoundary.clone();
			if(tmpWorldNodeBounadry.isValid())
				tmpWorldNodeBounadry.transform(tmpWorldMatrix);
			setWorldNodeBoundary(tmpWorldNodeBounadry);

			// Leave WorldTreeBoundary untouched
			if(!worldMatrixChanged)
				return;

			// Initialize WorldTreeBoundary
			tmpWorldTreeBoundary = tmpWorldNodeBounadry.clone();
		}

		// Update WorldTreeBoundary
		for(eCEntity child : getChilds()) {
			child.updateChildDependencies();
			tmpWorldTreeBoundary.merge(child.worldTreeBoundary);
		}
		setWorldTreeBoundary(tmpWorldTreeBoundary);
		*/
		// @fon
	}

	/**
	 * <ol>
	 * <li>Berechnung der WorldTreeBoundary aus der eigenen WorldNodeBoundary und der
	 * WorldTreeBoundary der Childs</li>
	 * <li>updateParentDependencies() für Parent, wenn
	 * <ul>
	 * <li>Alte WorldTreeBoundary teilweise 'auf' der WorldTreeBoundary des Parents liegt</li>
	 * <li>Neue WorldTreeBoundary nicht innerhalb der WorldTreeBoundary des Parents liegt</li>
	 * </ul>
	 * </li>
	 * </ol>
	 */
	public void updateParentDependencies() {
		bCBox tmpWorldTreeBoundary = worldNodeBoundary.clone();
		// Calc WorldTreeBoundary
		for (eCEntity child : getChilds()) {
			tmpWorldTreeBoundary.merge(child.worldTreeBoundary);
		}
		setWorldTreeBoundary(tmpWorldTreeBoundary);

		eCEntity parent = getParent();
		if (parent != null) {
			parent.updateParentDependencies();
		} else {
			// Context box of context is updated when the file containing the root entity is saved.
		}

		// Originale Gothic 3 Implementierung
		// @foff
		/*bCBox tmpWorldTreeBoundary = worldNodeBoundary.clone();
		// Calc WorldTreeBoundary
		for(eCEntity child : getChilds()) {
			tmpWorldTreeBoundary.merge(child.worldTreeBoundary);
		}

		eCEntity parent = getParent();
		if(parent != null) {
			// Old WorldTreeBoundary of this entity lies on border of the parent entity, so it is possible that the new boundary can shrink.
			if(worldTreeBoundary.getMin().getX() == parent.worldTreeBoundary.getMin().getX()
			|| worldTreeBoundary.getMin().getY() == parent.worldTreeBoundary.getMin().getY()
			|| worldTreeBoundary.getMin().getZ() == parent.worldTreeBoundary.getMin().getZ()
			|| worldTreeBoundary.getMax().getX() == parent.worldTreeBoundary.getMax().getX()
			|| worldTreeBoundary.getMax().getY() == parent.worldTreeBoundary.getMax().getY()
			|| worldTreeBoundary.getMax().getZ() == parent.worldTreeBoundary.getMax().getZ()) {
				setWorldTreeBoundary(tmpWorldTreeBoundary);
				parent.updateParentDependencies();
			} else {
				setWorldTreeBoundary(tmpWorldTreeBoundary);
				if(!parent.worldTreeBoundary.contains(tmpWorldTreeBoundary)) {
					// TODO: Bug in G3 Code?! In some situatuions the TreeBoundary of the parent of the parent has to updated!
					// Test original data!
					parent.setWorldTreeBoundary(tmpWorldTreeBoundary.getMerged(parent.worldTreeBoundary));
				}

			}
		} else {
			setWorldTreeBoundary(tmpWorldTreeBoundary);
			// TODO: Context
			// currentContext.setContextBox(tmpWorldTreeBoundary);
		}*/
		// @fon
	}

	public boolean hasParent() {
		return parent != null;
	}

	public eCEntity getParent() {
		return parent;
	}

	public void setParent(eCEntity parent) {
		this.parent = parent;
	}

	@SuppressWarnings("unchecked")
	public <T extends eCEntity> List<T> getChilds() {
		return (List<T>) Collections.unmodifiableList(childs);
	}

	public FluentIterable<eCEntity> getIndirectChilds() {
		return EntityTreeTraverser.traversePreOrder(this).filter(e -> e != this);
	}

	/**
	 * Child {@code child} hinzufügen (und zuvor von vorherigen Parent entfernen), und entsprechend
	 * {@code updateParentDependencies} die ParentDependencies dieser Entity (und des vorherigen
	 * Parents) aktualisieren
	 *
	 * @param child
	 */
	public void attachChild(eCEntity child) {
		if (child.hasParent()) {
			child.moveToNode(this);
		} else {
			child.setParent(this);
			childs.add(child);
		}
	}

	/**
	 * Child {@code child} entfernen, und entsprechend {@code updateParentDependencies} die
	 * ParentDependencies dieser Entity aktualisieren
	 *
	 * @param child
	 */
	public void detachChild(eCEntity child) {
		child.setParent(null);
		childs.remove(child);
	}

	/**
	 * Alle Childs entfernen, und entsprechend {@code updateParentDependencies} die
	 * ParentDependencies dieser Entity aktualisieren
	 *
	 * @param updateParentDependencies
	 */
	public void removeAllChildren(boolean updateParentDependencies) {
		childs.forEach(child -> child.setParent(null));
		childs.clear();
		if (updateParentDependencies) {
			updateParentDependencies();
		}
	}

	/**
	 * Entfernen dieser Entity von ihrem Parent, und entsprechend {@code updateParentDependencies}
	 * die ParentDependencies des Parents aktualisieren
	 *
	 * @param updateParentDependencies
	 */
	public void removeFromParent(boolean updateParentDependencies) {
		if (hasParent()) {
			eCEntity parentEntity = getParent();
			parentEntity.detachChild(this);
			if (updateParentDependencies) {
				parentEntity.updateParentDependencies();
			}
		}
	}

	private void moveToNode(eCEntity target) {
		removeFromParent(false);
		target.attachChild(this);
	}

	/**
	 * Lokale Koordinaten der Entity bleiben unverändert (anpassen der WorldMatrix an neuen Parent)
	 *
	 * @param target
	 */
	public void moveToLocalNode(eCEntity target) {
		removeFromParent(true);
		target.attachChild(this);
		updateGeometry();
		target.updateGeometry();
	}

	/**
	 * Weltkoordinaten der Entity bleiben unverändert (anpassen der LocalMatrix an neuen Parent)
	 *
	 * @param target
	 */
	public void moveToWorldNode(eCEntity target) {
		removeFromParent(true);
		target.attachChild(this);
		setLocalMatrix(target.worldMatrix.getInverted().getProduct(worldMatrix));
	}

	public boolean isChild(eCEntity child) {
		return getChilds().contains(child);
	}

	public boolean isIndirectChild(eCEntity child) {
		return EntityTreeTraverser.traversePreOrder(this).anyMatch(c -> c == child && c != this);
	}

	public Optional<eCEntity> getChildByGuid(String guid) {
		return childs.stream().filter(c -> c.getGuid().equals(guid)).findAny();
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isRenderingEnabled() {
		return renderingEnabled;
	}

	public void setRenderingEnabled(boolean renderingEnabled) {
		this.renderingEnabled = renderingEnabled;
	}

	public boolean isProcessingDisabled() {
		return processingDisabled;
	}

	public void setProcessingDisabled(boolean processingDisabled) {
		this.processingDisabled = processingDisabled;
	}

	public boolean isDeactivationEnabled() {
		return deactivationEnabled;
	}

	public void setDeactivationEnabled(boolean deactivationEnabled) {
		this.deactivationEnabled = deactivationEnabled;
	}

	public boolean isPickable() {
		return pickable;
	}

	public void setPickable(boolean pickable) {
		this.pickable = pickable;
	}

	public boolean isCollisionEnabled() {
		return collisionEnabled;
	}

	public void setCollisionEnabled(boolean collisionEnabled) {
		this.collisionEnabled = collisionEnabled;
	}

	public float getRenderAlphaValue() {
		return renderAlphaValue;
	}

	public void setRenderAlphaValue(float renderAlphaValue) {
		this.renderAlphaValue = renderAlphaValue;
	}

	public int getInsertType() {
		return insertType;
	}

	public void setInsertType(int insertType) {
		this.insertType = insertType;
	}

	public int getLastRenderPriority() {
		return lastRenderPriority;
	}

	public void setLastRenderPriority(int lastRenderPriority) {
		this.lastRenderPriority = lastRenderPriority;
	}

	public boolean isSpecialDepthTexPassEnabled() {
		return specialDepthTexPassEnabled;
	}

	public void setSpecialDepthTexPassEnabled(boolean specialDepthTexPassEnabled) {
		this.specialDepthTexPassEnabled = specialDepthTexPassEnabled;
	}

	public float getVisualLoDFactor() {
		return visualLoDFactor;
	}

	public void setVisualLoDFactor(float visualLoDFactor) {
		this.visualLoDFactor = visualLoDFactor;
	}

	public float getObjectCullFactor() {
		return objectCullFactor;
	}

	public void setObjectCullFactor(float objectCullFactor) {
		this.objectCullFactor = objectCullFactor;
	}

	public long getDataChangedTimeStamp() {
		return dataChangedTimeStamp;
	}

	public void setDataChangedTimeStamp(long dataChangedTimeStamp) {
		this.dataChangedTimeStamp = dataChangedTimeStamp;
	}

	public float getUniformScaling() {
		return uniformScaling;
	}

	public boolean isProcessingRangeOutFadingEnabled() {
		return processingRangeOutFadingEnabled;
	}

	public void setProcessingRangeOutFadingEnabled(boolean processingRangeOutFadingEnabled) {
		this.processingRangeOutFadingEnabled = processingRangeOutFadingEnabled;
	}

	/**
	 * eSEntityFlags:__FIXME_13 @ 0x2000 | Must Compiled Static ?
	 */
	public boolean isUnkFlag2() {
		return unkFlag2;
	}

	public void setUnkFlag2(boolean unkFlag2) {
		this.unkFlag2 = unkFlag2;
	}

	/**
	 * eSEntityFlags:__FIXME_22 @ 0x400000 | Derived Template ?
	 */
	public boolean isUnkFlag3() {
		return unkFlag3;
	}

	public void setUnkFlag3(boolean unkFlag3) {
		this.unkFlag3 = unkFlag3;
	}

	/**
	 * eSEntityFlags:__FIXME_27 @ 0x8000000 | Ranged Object Culling ?
	 */
	public boolean isRangedObjectCulling() {
		return rangedObjectCulling;
	}

	public void setRangedObjectCulling(boolean rangedObjectCulling) {
		this.rangedObjectCulling = rangedObjectCulling;
	}

	public boolean hasCreator() {
		return getCreator() != null;
	}

	/**
	 * @return bCPropertyID von eCTemplateEntity
	 */
	public abstract String getCreator();

	/**
	 * @param creator bCPropertyID von eCTemplateEntity
	 */
	public abstract void setCreator(String creator);

	public abstract void read(G3FileReader reader, boolean skipPropertySets);

	public abstract void write(G3FileWriter writer);

	@Override
	public eCEntity clone() {
		G3FileWriterEx writer = new G3FileWriterEx();
		write(writer);

		G3FileReaderEx reader = new G3FileReaderEx(writer.getData());
		reader.setStringtable(writer.getStringtable());
		eCEntity result = newInstance(false);
		result.read(reader, false);
		return result;
	}

	protected abstract eCEntity newInstance(boolean initialize);

	public final void createFromTemplate(TemplateEntity tple, Function<String, TemplateEntity> tpleLookup) {
		onCreateFromTemplate(tple, tpleLookup);
		onChildrenAvailable(tpleLookup);
	}

	protected void onCreateFromTemplate(TemplateEntity tple, Function<String, TemplateEntity> tpleLookup) {
		onPatchWithTemplate(tple, true, false, tpleLookup);
		for (TemplateEntity childTple : tple.<TemplateEntity>getChilds()) {
			eCEntity child = newInstance(true);
			attachChild(child);
			child.onCreateFromTemplate(childTple, tpleLookup);
		}
	}

	protected void onPatchWithTemplate(TemplateEntity tple, boolean justCreated, boolean dontCallOnChildrenAvailable,
			Function<String, TemplateEntity> tpleLookup) {
		if (tple.isDeleted()) {
			throw new IllegalStateException("Patching mit als gelöscht markierter Template nicht möglich.");
		} else if (justCreated || getDataChangedTimeStamp() != tple.getDataChangedTimeStamp() || getDataChangedTimeStamp() == -1) {
			tple.patchEntity(this, justCreated, dontCallOnChildrenAvailable, tpleLookup);
		} else {
			setCreator(tple.getGuid());
		}
	}

	private void onChildrenAvailable(Function<String, TemplateEntity> tpleLookup) {
		TemplateEntity creator = null;
		if (hasCreator()) {
			creator = tpleLookup.apply(getCreator());
			if (creator == null) {
				throw new IllegalStateException("Creator-Template " + getCreator() + " konnte nicht geladen werden.");
			}
		}

		if (!hasCreator() || getDataChangedTimeStamp() != creator.getDataChangedTimeStamp()) {
			for (G3Class propertySet : getClasses()) {
				if (hasCreator()) {
					propertySet.onChildrenAvailable(getDataChangedTimeStamp(), creator.getDataChangedTimeStamp(), creator);
				} else {
					propertySet.onChildrenAvailable(getDataChangedTimeStamp(), -1, null);
				}
			}

			if (hasCreator()) {
				setDataChangedTimeStamp(creator.getDataChangedTimeStamp());
			}
		}
	}

	public void copyEntityPrivateData(eCEntity entity, boolean justCreated) {
		enabled = entity.enabled;
		renderingEnabled = entity.renderingEnabled;
		processingDisabled = entity.processingDisabled;
		deactivationEnabled = entity.deactivationEnabled;
		pickable = entity.pickable;
		collisionEnabled = entity.collisionEnabled;
		renderAlphaValue = entity.renderAlphaValue;
		insertType = entity.insertType;
		lastRenderPriority = entity.lastRenderPriority;
		specialDepthTexPassEnabled = entity.specialDepthTexPassEnabled;
		unkFlag2 = entity.unkFlag2;

		visualLoDFactor = entity.visualLoDFactor;
		unkFlag3 = entity.unkFlag3;
		objectCullFactor = entity.objectCullFactor;
		rangedObjectCulling = entity.rangedObjectCulling;
		processingRangeOutFadingEnabled = entity.processingRangeOutFadingEnabled;

		if (justCreated) {
			name = entity.name;
			worldMatrix = entity.worldMatrix.clone();
			localMatrix = entity.localMatrix.clone();
			worldTreeBoundary = entity.worldTreeBoundary.clone();
			localNodeBoundary = entity.localNodeBoundary.clone();
			worldNodeBoundary = entity.worldNodeBoundary.clone();
			worldTreeSphere = entity.worldTreeSphere.clone();
			worldNodeSphere = entity.worldNodeSphere.clone();
			uniformScaling = entity.uniformScaling;
		}
	}

	@Override
	public String toString() {
		String name = getName();
		if (name.isEmpty()) {
			name = EntityUtil.getMesh(this).orElse(null);
		}
		if (name == null) {
			name = EntityUtil.getTreeMesh(this).orElse(null);
		}
		if (name == null && hasClass(CD.eCVegetation_PS.class)) {
			name = "<Vegetation Root>";
		}
		if (name == null) {
			name = "<no name>";
		}
		return name != null ? name : "";
	}
}
