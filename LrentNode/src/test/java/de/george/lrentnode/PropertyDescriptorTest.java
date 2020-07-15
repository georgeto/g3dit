package de.george.lrentnode;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SortedSetMultimap;

import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.properties.PropertyInstantiator;
import de.george.lrentnode.properties.Unknown;
import de.george.lrentnode.properties.gShort;
import de.george.lrentnode.properties.gUnsignedShort;

public class PropertyDescriptorTest {
	@Test
	public void testInstantiatePropertyDescriptors() throws Exception {
		for (Class<?> clazz : CD.class.getDeclaredClasses()) {
			for (Field field : clazz.getDeclaredFields()) {
				PropertyDescriptor<?> propDesc = (PropertyDescriptor<?>) field.get(null);

				Class<? extends G3Serializable> instanceType = PropertyInstantiator
						.getPropertyInstance(propDesc.getName(), propDesc.getDataTypeName()).getClass();

				// Unsigned qualifier is lost during serialization, for details see
				// PropertyDescriptor.
				if (propDesc.getDataType() == gUnsignedShort.class) {
					Assert.assertEquals(instanceType, gShort.class);
				} else {
					Assert.assertEquals(propDesc.getDataType(), instanceType);
				}
			}
		}
	}

	@Test
	public void testGeneratePropertyDescriptors() throws Exception {
		SortedSetMultimap<String, String> regs = MultimapBuilder.treeKeys().treeSetValues().build();
		for (Class<?> clazz : CD.class.getDeclaredClasses()) {
			for (Field field : clazz.getDeclaredFields()) {
				PropertyDescriptor<?> propDesc = (PropertyDescriptor<?>) field.get(null);
				G3Serializable instance = PropertyInstantiator.getPropertyInstance(propDesc.getName(), propDesc.getDataTypeName());
				Assert.assertNotNull(instance);
				if (instance instanceof Unknown) {
					regs.put(propDesc.getDataType().getSimpleName(), propDesc.getDataTypeName());
				}
			}
		}
		regs.keySet().forEach(r -> System.out.println("add(" + r + ".class, \"" + Joiner.on("\", \"").join(regs.get(r)) + "\");"));
	}
}
