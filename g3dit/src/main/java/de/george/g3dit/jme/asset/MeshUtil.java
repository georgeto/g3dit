package de.george.g3dit.jme.asset;

import static de.george.lrentnode.enums.G3Enums.eEVertexStreamArrayType.eEVertexStreamArrayType_Face;
import static de.george.lrentnode.enums.G3Enums.eEVertexStreamArrayType.eEVertexStreamArrayType_Normal;
import static de.george.lrentnode.enums.G3Enums.eEVertexStreamArrayType.eEVertexStreamArrayType_TextureCoordinate;
import static de.george.lrentnode.enums.G3Enums.eEVertexStreamArrayType.eEVertexStreamArrayType_VertexPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.archive.animation.Chunks.LMA_CHUNK;
import de.george.lrentnode.archive.animation.Chunks.MaterialChunk;
import de.george.lrentnode.archive.animation.Chunks.MeshChunk;
import de.george.lrentnode.archive.animation.Chunks.Submesh;
import de.george.lrentnode.archive.animation.Chunks.Vertex;
import de.george.lrentnode.archive.animation.eCResourceAnimationActor_PS;
import de.george.lrentnode.archive.animation.eCResourceAnimationActor_PS.eCWrapper_emfx2Actor.MaterialReference;
import de.george.lrentnode.classes.eCResourceMeshComplex_PS;
import de.george.lrentnode.structures.eCMeshElement;
import one.util.streamex.StreamEx;

public class MeshUtil {
	private static final Logger logger = LoggerFactory.getLogger(MeshUtil.class);

	public static class IllegalMeshException extends Exception {
		private int meshIndex;
		private String materialName;

		public IllegalMeshException(int meshIndex, String materialName, String message) {
			super(String.format("Invalid SubMesh %d (%s): %s", meshIndex, materialName, message));
			this.meshIndex = meshIndex;
			this.materialName = materialName;
		}

		public int getMeshIndex() {
			return meshIndex;
		}

		public String getMaterialName() {
			return materialName;
		}
	}

	public static final List<IntermediateMesh> toIntermediateMesh(eCResourceMeshComplex_PS sourceComplexMesh) throws IllegalMeshException {
		return toIntermediateMesh(sourceComplexMesh, false);
	}

	public static final List<IntermediateMesh> toIntermediateMesh(eCResourceMeshComplex_PS sourceComplexMesh, boolean onlyMaterial)
			throws IllegalMeshException {
		List<IntermediateMesh> meshes = new ArrayList<>();

		for (int i = 0; i < sourceComplexMesh.meshElements.size(); i++) {
			eCMeshElement sourceMesh = sourceComplexMesh.meshElements.get(i);
			IntermediateMesh mesh = new IntermediateMesh();
			mesh.materialName = sourceMesh.getMaterialName();

			if (!onlyMaterial) {
				// Faces
				if (sourceMesh.hasStreamArray(eEVertexStreamArrayType_Face)) {
					List<Integer> faceIndicies = sourceMesh.getStreamArrayByType(eEVertexStreamArrayType_Face);
					if (faceIndicies.size() % 3 != 0) {
						throw new IllegalMeshException(i, mesh.materialName, "Number of face indices is not am multiple of 3.");
					}
					mesh.indices = faceIndicies;
				}

				// Vertices
				if (sourceMesh.hasStreamArray(eEVertexStreamArrayType_VertexPosition)) {
					List<bCVector> vertices = sourceMesh.getStreamArrayByType(eEVertexStreamArrayType_VertexPosition);
					mesh.vertices = vertices;
				}

				// Normals
				if (sourceMesh.hasStreamArray(eEVertexStreamArrayType_Normal)) {
					List<bCVector> normals = sourceMesh.getStreamArrayByType(eEVertexStreamArrayType_Normal);
					mesh.normals = normals;
				}

				// Texture coordinates
				if (sourceMesh.hasStreamArray(eEVertexStreamArrayType_TextureCoordinate)) {
					mesh.texCoords = sourceMesh.getStreamArrayByType(eEVertexStreamArrayType_TextureCoordinate);
				}
			}

			meshes.add(mesh);
		}

		return meshes;
	}

	public static final List<IntermediateMesh> toIntermediateMesh(eCResourceAnimationActor_PS sourceActor) throws IllegalMeshException {
		return toIntermediateMesh(sourceActor, false);
	}

	public static final List<IntermediateMesh> toIntermediateMesh(eCResourceAnimationActor_PS sourceActor, boolean onlyMaterial)
			throws IllegalMeshException {
		List<IntermediateMesh> meshes = new ArrayList<>();

		List<MeshChunk> chunks = sourceActor.actor.getChunksByType(LMA_CHUNK.LMA_CHUNK_MESH);
		for (MeshChunk chunk : chunks) {
			List<MaterialReference> materials = sourceActor.actor.materials;
			List<MaterialChunk> materialChunks = sourceActor.actor.getChunksByType(LMA_CHUNK.LMA_CHUNK_MATERIAL);
			for (int i = 0; i < chunk.submeshes.size(); i++) {
				Submesh submesh = chunk.submeshes.get(i);
				IntermediateMesh mesh = new IntermediateMesh();

				Optional<MaterialReference> material = StreamEx.of(materials).findFirst(m -> m.matIndex == submesh.matID);
				// We don't go all the way and check whether the material exists, but instead test
				// for an empty material name as approximation (sufficient for meshes created by
				// Rimy3D).
				if (material.isPresent() && !material.get().name.isEmpty()) {
					mesh.materialName = material.get().name;
				} else
				// If Gothic 3 fails to resolve / load at least one material in the
				// actors MaterialsLoDMappings, it uses the material chunks of the actor to
				// recreate the MaterialsLoDMappings.
				if (submesh.matID >= 0 && submesh.matID < materialChunks.size()) {
					mesh.materialName = materialChunks.get(submesh.matID).materialName.replace("|", "") + ".xshmat";
				} else {
					throw new IllegalMeshException(i, Integer.toString(submesh.matID), "Reference to non exisiting material.");
				}

				if (!onlyMaterial) {
					if (submesh.numUVSets > 1) {
						logger.warn("Submesh {} ({}) has multiple %d UV sets.", i, mesh.materialName, submesh.numUVSets);
					}

					boolean hasTexCoords = submesh.numUVSets >= 1;
					for (Vertex vertex : submesh.vertices) {
						mesh.addVertex(vertex.getPositionXYZ());
						mesh.addNormal(vertex.getNormalXYZ());
						if (hasTexCoords) {
							mesh.addTexCoord(vertex.uvSets.get(0));
						}
					}

					// Faces
					if (submesh.indices.size() % 3 != 0) {
						throw new IllegalMeshException(i, mesh.materialName, "Number of face indices is not am multiple of 3.");
					}

					mesh.indices = submesh.indices;
				}

				meshes.add(mesh);
			}
		}

		return meshes;
	}

}
