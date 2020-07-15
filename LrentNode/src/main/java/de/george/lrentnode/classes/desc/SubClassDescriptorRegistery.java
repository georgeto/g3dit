package de.george.lrentnode.classes.desc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableListMultimap;

import de.george.lrentnode.classes.eCCollisionShape_PS;
import de.george.lrentnode.classes.gCInventory_PS;

public class SubClassDescriptorRegistery {
	// Initialization-on-demand holder idiom
	private static class Holder {
		static final SubClassDescriptorRegistery INSTANCE = new SubClassDescriptorRegistery();
	}

	public static SubClassDescriptorRegistery getInstance() {
		return Holder.INSTANCE;
	}

	private ImmutableListMultimap<Class<? extends ClassDescriptor>, SubClassDescriptor> subclasses;

	private SubClassDescriptorRegistery() {
		init();
	}

	private void init() {
		subclasses = ImmutableListMultimap.<Class<? extends ClassDescriptor>, SubClassDescriptor>builder()
				.put(CD.eCCollisionShape_PS.class,
						new SubClassDescriptor("Shapes", "Shape", CD.eCCollisionShape.class, CD.eCCollisionShape_PS.class,
								eCCollisionShape_PS::getShapes))
				.put(CD.gCInventory_PS.class,
						new SubClassDescriptor("Stacks", "Stack", CD.gCInventoryStack.class, CD.gCInventory_PS.class,
								gCInventory_PS::getStacks))
				.put(CD.gCInventory_PS.class, new SubClassDescriptor("Slots", "Slot", CD.gCInventorySlot.class, CD.gCInventory_PS.class,
						gCInventory_PS::getSlots))
				.build();
	}

	public List<SubClassDescriptor> lookupSubClasses(Class<? extends ClassDescriptor> propertySet) {
		return subclasses.get(propertySet);
	}

	public List<SubClassDescriptor> lookupSubClasses(String propertySetName) {
		Optional<Class<? extends ClassDescriptor>> propertySet = PropertyDescriptorRegistry.getInstance()
				.lookupPropertySet(propertySetName);
		return propertySet.isPresent() ? lookupSubClasses(propertySet.get()) : Collections.emptyList();
	}
}
