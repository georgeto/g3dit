package de.george.lrentnode.diff;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import de.danielbechler.diff.ObjectDiffer;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.access.Instances;
import de.danielbechler.diff.access.PropertyAwareAccessor;
import de.danielbechler.diff.identity.IdentityService;
import de.danielbechler.diff.identity.IdentityStrategy;
import de.danielbechler.diff.inclusion.Inclusion;
import de.danielbechler.diff.inclusion.InclusionResolver;
import de.danielbechler.diff.introspection.PropertyAccessorFilter;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.DiffNode.State;
import de.george.g3utils.io.G3FileWriterEx;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.util.ReflectionUtils;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.archive.lrentdat.LrentdatEntity;
import de.george.lrentnode.archive.node.NodeEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.eCVegetation_PS.eCVegetation_GridNode;
import de.george.lrentnode.classes.eCVisualAnimation_PS;
import de.george.lrentnode.diff.introspection.FieldAccessor;
import de.george.lrentnode.diff.introspection.FieldIntrospector;
import de.george.lrentnode.properties.ClassProperty;
import de.george.lrentnode.properties.bCPropertyID;
import de.george.lrentnode.properties.bCString;
import de.george.lrentnode.template.TemplateEntity;

public class EntityDiffer {
	private ObjectDiffer differ;
	private IdentityService identityService;
	private Map<Class<?>, IdentityStrategy> matchingStrategies;
	private Multimap<Class<?>, String> exclusions;
	private boolean stopOnAddedOrRemoved;

	public EntityDiffer(boolean stopOnAddedOrRemoved) {
		this.stopOnAddedOrRemoved = stopOnAddedOrRemoved;
		initMatchingStrategies();
		initExclusions();
		initObjectDiffer();
	}

	private void addMethodMatchingStrategy(Class<?> type, String methodName) {
		matchingStrategies.put(type, new EqualsProviderMethodIdentityStrategy(methodName));
	}

	private void addFieldMatchingStrategy(Class<?> type, String fieldName) {
		matchingStrategies.put(type, new EqualsProviderFieldIdentityStrategy(fieldName));
	}

	private void initExclusions() {
		exclusions = HashMultimap.create();
		exclusions.put(eCEntity.class, "parent");
		// exclusions.put(eCEntity.class, "childs");
		exclusions.put(eCEntity.class, "file");
		exclusions.put(G3Class.class, "deadcodePosition");
	}

	private void initMatchingStrategies() {
		matchingStrategies = new ConcurrentHashMap<>();
		addMethodMatchingStrategy(G3Class.class, "getClassName");
		addMethodMatchingStrategy(ClassProperty.class, "getName");
		addMethodMatchingStrategy(bCPropertyID.class, "getGuid");
		addMethodMatchingStrategy(bCString.class, "getString");
		addFieldMatchingStrategy(eCVisualAnimation_PS.MaterialSwitchSlot.class, "name");
		addFieldMatchingStrategy(eCVisualAnimation_PS.ExtraSlot.class, "name");
		addMethodMatchingStrategy(eCEntity.class, "getGuid");
		addMethodMatchingStrategy(eCVegetation_GridNode.class, "getIndex");
		matchingStrategies.put(G3Serializable.class, new BeanEqualsIdentityStrategy());
	}

	private void initObjectDiffer() {
		FieldIntrospector fieldIntrospector = new FieldIntrospector();
		fieldIntrospector.setReturnFinalFields(true);

		ObjectDifferBuilder builder = ObjectDifferBuilder.startBuilding();
		identityService = builder.identity();
		builder.introspection().setDefaultIntrospector(fieldIntrospector);

		EntityBinaryAccesorFilter accesorFilter = new EntityBinaryAccesorFilter();
		builder.introspection().ofType(TemplateEntity.class).toFilter(accesorFilter);
		builder.introspection().ofType(NodeEntity.class).toFilter(accesorFilter);
		builder.introspection().ofType(LrentdatEntity.class).toFilter(accesorFilter);
		builder.inclusion().resolveUsing(new InclusionResolver() {
			@Override
			public Inclusion getInclusion(DiffNode node) {
				DiffNode parentNode = node.getParentNode();
				if (parentNode != null) {
					if (stopOnAddedOrRemoved) {
						State parentState = parentNode.getState();
						if (parentState == State.ADDED || parentState == State.REMOVED) {
							return Inclusion.EXCLUDED;
						}
					}

					Class<?> parentType = parentNode.getValueType();
					for (Class<?> regType : exclusions.keySet()) {
						if (regType.isAssignableFrom(parentType)) {
							if (exclusions.get(regType).contains(node.getPropertyName())) {
								return Inclusion.EXCLUDED;
							}
						}
					}
				}
				return Inclusion.DEFAULT;
			}

			@Override
			public boolean enablesStrictIncludeMode() {
				return false;
			}
		});
		builder.collectionMatchingStrategyResolver(type -> {
			Class<?> bestType = null;
			for (Class<?> strategyType : matchingStrategies.keySet()) {
				if (strategyType.isAssignableFrom(type)) {
					if (bestType == null || bestType.isAssignableFrom(strategyType)) {
						bestType = strategyType;
					}
				}
			}
			return bestType != null ? matchingStrategies.get(bestType) : null;
		});
		differ = builder.build();
	}

	public DiffNode diff(eCEntity working, eCEntity base) {
		return differ.compare(working, base);
	}

	private static class EntityBinaryAccesorFilter implements PropertyAccessorFilter {
		private static final List<PropertyAwareAccessor> CHILDS_ACCESSOR = ImmutableList
				.of(new FieldAccessor(ReflectionUtils.getField(eCEntity.class, "childs")));

		@Override
		public Collection<PropertyAwareAccessor> filter(DiffNode node, Instances instances) {
			eCEntity base = instances.getBase(eCEntity.class);
			eCEntity working = instances.getWorking(eCEntity.class);
			if (base != null && working != null) {
				G3FileWriterEx baseBinary = new G3FileWriterEx();
				base.write(baseBinary);
				baseBinary.writeStringtable();
				G3FileWriterEx workingBinary = new G3FileWriterEx();
				working.write(workingBinary);
				workingBinary.writeStringtable();

				ByteBuffer baseBuffer = baseBinary.getBuffer();
				baseBuffer.flip();
				ByteBuffer workingBuffer = workingBinary.getBuffer();
				workingBuffer.flip();
				if (baseBuffer.equals(workingBuffer)) {
					return CHILDS_ACCESSOR;
				}
			}

			return node.getValueTypeInfo().getAccessors();
		}
	}
}
