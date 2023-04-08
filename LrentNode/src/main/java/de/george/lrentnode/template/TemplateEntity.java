package de.george.lrentnode.template;

import java.util.ArrayList;
import java.util.function.Function;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCSphere;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.util.ClassUtil;

public class TemplateEntity extends eCEntity {
	protected String fileName;
	protected boolean helperParent; // Item-Header (Hash e.g. used in .lrtpldatasc)
	protected String refTemplate; // Used in Object-Groups
	protected boolean deleted;

	public TemplateEntity(boolean initialize) {
		super(initialize);
		if (initialize) {
			invalidate();
		}
	}

	private void invalidate() {
		fileName = "";
		refTemplate = null;
		deleted = false;
		helperParent = false;
		dataChangedTimeStamp = 0;

		// @foff
		/*
		 * Set
		 * 	m_Enabled
		 * 	m_DeactivationEnabled
		 * 	m_ProcessingRangeEntered
		 *  m_Pickable
		 *  m_CollisionEnabled
		 *
		 * Kept
		 * 	m_InsertType
		 *  __FIXME_24
		 * 	m_SpecialDepthTexPassEnabled
		 *  m_Processable
		 *  __FIXME_27
		 *  m_ProcessingRangeOutFadingEnabled
		 *  m_Temporary
		 *  __FIXME_30
		 *  __FIXME_31
		 */
		// @fon
		enabled = true;
		renderingEnabled = false;
		processingDisabled = false;
		deactivationEnabled = true;
		pickable = true;
		collisionEnabled = true;
		unkFlag2 = false;
		unkFlag3 = false;
	}

	@Override
	public void read(G3FileReader reader, boolean skipPropertySets) {
		int headerVersion = reader.readUnsignedShort();

		fileName = reader.readEntry();
		setGuid(reader.readGUID());
		enabled = reader.readBool();
		renderingEnabled = reader.readBool();
		processingDisabled = reader.readBool();
		deactivationEnabled = reader.readBool();
		pickable = reader.readBool();
		collisionEnabled = reader.readBool();
		unkFlag2 = reader.readBool();
		helperParent = reader.readBool();
		if (reader.readBool()) {
			refTemplate = reader.readGUID();
		}
		renderAlphaValue = reader.readFloat();
		insertType = reader.readUnsignedShort();
		lastRenderPriority = reader.readUnsignedByte();

		name = reader.readEntry();
		worldMatrix = reader.read(bCMatrix.class);
		localMatrix = reader.read(bCMatrix.class);
		worldTreeBoundary = reader.readBox();
		localNodeBoundary = reader.readBox();
		worldNodeBoundary = reader.readBox();
		worldNodeSphere = new bCSphere();
		worldTreeSphere = new bCSphere();

		visualLoDFactor = reader.readFloat();
		unkFlag3 = reader.readBool();
		deleted = reader.readBool();

		if (headerVersion >= 40) {
			objectCullFactor = reader.readFloat();
		} else {
			objectCullFactor = 1.0f;
		}

		if (headerVersion >= 56) {
			dataChangedTimeStamp = reader.readUnsignedInt();
		} else {
			dataChangedTimeStamp = 0;
		}

		if (headerVersion >= 58) {
			specialDepthTexPassEnabled = reader.readBool();
		} else {
			specialDepthTexPassEnabled = false;
		}

		if (headerVersion >= 61) {
			uniformScaling = reader.readFloat();
		} else {
			uniformScaling = worldMatrix.getPureScaling().getX();
		}

		if (headerVersion >= 62) {
			rangedObjectCulling = reader.readBool();
			processingRangeOutFadingEnabled = reader.readBool();
		} else {
			rangedObjectCulling = false;
			processingRangeOutFadingEnabled = false;
		}

		// Only if header is not referencing to an other header
		if (refTemplate == null) {
			int numberClassEntries = reader.readInt();
			for (int i = 0; i < numberClassEntries; i++) {
				addClass(ClassUtil.readTemplateClass(reader));
			}
		}
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeUnsignedShort(62);
		writer.writeEntry(fileName);
		writer.write(getGuid());
		writer.writeBool(enabled);
		writer.writeBool(renderingEnabled);
		writer.writeBool(processingDisabled);
		writer.writeBool(deactivationEnabled);
		writer.writeBool(pickable);
		writer.writeBool(collisionEnabled);
		writer.writeBool(unkFlag2);
		writer.writeBool(helperParent);
		writer.writeBool(refTemplate != null);
		if (refTemplate != null) {
			writer.write(refTemplate);
		}

		writer.writeFloat(renderAlphaValue);
		writer.writeUnsignedShort(insertType);
		writer.writeUnsignedByte(lastRenderPriority);
		writer.writeEntry(name);

		writer.write(worldMatrix);
		writer.write(localMatrix);
		writer.writeBox(worldTreeBoundary);
		writer.writeBox(localNodeBoundary);
		writer.writeBox(worldNodeBoundary);

		writer.writeFloat(visualLoDFactor);
		writer.writeBool(unkFlag3);
		writer.writeBool(deleted);
		writer.writeFloat(objectCullFactor);
		writer.writeUnsignedInt(dataChangedTimeStamp);
		writer.writeBool(specialDepthTexPassEnabled);
		writer.writeFloat(uniformScaling);
		writer.writeBool(rangedObjectCulling);
		writer.writeBool(processingRangeOutFadingEnabled);

		// Only if header is not referencing to an other header
		if (refTemplate == null) {
			writeClasses(ClassUtil::writeTemplateClass, writer);
		}
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isHelperParent() {
		return helperParent;
	}

	public void setHelperParent(boolean helperParent) {
		this.helperParent = helperParent;
	}

	public String getRefTemplate() {
		return refTemplate;
	}

	public void setRefTemplate(String refTemplate) {
		this.refTemplate = refTemplate;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public String getCreator() {
		return refTemplate;
	}

	@Override
	public void setCreator(String refTemplate) {
		this.refTemplate = refTemplate;
	}

	@Override
	protected eCEntity newInstance(boolean initialize) {
		return new TemplateEntity(initialize);
	}

	public void patchEntity(eCEntity entity, boolean justCreated, boolean dontCallOnChildrenAvailable,
			Function<String, TemplateEntity> tpleLookup) {
		if (!justCreated) {
			throw new UnsupportedOperationException("Patching of existing entities is not yet supported.");
		}

		if (refTemplate == null) {
			for (G3Class propertySet : getClasses()) {
				G3Class entityPropertySet = entity.getClass(propertySet.getClassName());
				// State of PropertySet is changing, and is therefore not represented by Template.
				// Patching can be implemented in OnCustomPatch function.
				if (!justCreated /* && !propertySet.isReferencedByTemplate() */) {
					if (entityPropertySet != null) {
						// entityPropertySet.onCustomPatch();
						continue;
					}
				} else if (entityPropertySet != null) {
					entity.removeClass(entityPropertySet.getClassName());
				}

				// if(propertySet.isCopyable()), but is true for everything except of
				// eCEditorEntityPropertySet
				entity.addClass(ClassUtil.clone(propertySet), getClassVersion(propertySet));
			}

			for (G3Class entityPropertySet : new ArrayList<>(entity.getClasses())) {
				if (this.getClass(entityPropertySet.getClassName()) == null) {
					entity.removeClass(entityPropertySet.getClassName());
				}
			}

			entity.copyEntityPrivateData(this, justCreated);
			entity.setUnkFlag3(true);
			entity.setCreator(getGuid());

			if (dontCallOnChildrenAvailable) {
				return;
			}

			for (G3Class propertySet : entity.getClasses()) {
				propertySet.onChildrenAvailable(entity.getDataChangedTimeStamp(), getDataChangedTimeStamp(), this);
			}
		} else {
			TemplateEntity refTpleEntity = tpleLookup.apply(refTemplate);
			if (refTpleEntity == null) {
				throw new IllegalStateException("RefTemplate " + refTemplate + " could not be loaded.");
			}
			refTpleEntity.patchEntity(entity, justCreated, false, tpleLookup);
			entity.copyEntityPrivateData(this, justCreated);
		}
	}
}
