package de.george.g3dit.jme.asset;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.List;
import java.util.logging.Logger;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.ModelKey;
import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.scene.mesh.IndexIntBuffer;
import com.jme3.scene.mesh.IndexShortBuffer;
import com.jme3.util.BufferUtils;

import de.george.g3dit.jme.asset.MeshUtil.IllegalMeshException;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;

public abstract class AbstractG3MeshLoader implements AssetLoader {

	private static final Logger logger = Logger.getLogger(AbstractG3MeshLoader.class.getName());

	protected AssetManager assetManager;

	private String objName;

	private int geomIndex;

	protected Geometry createGeometry(IntermediateMesh sourceMesh, int materialSwitch) throws IOException {
		if (sourceMesh.indices.isEmpty()) {
			throw new IOException("No geometry data to generate mesh");
		}

		// Create mesh from the faces
		Mesh mesh = constructMesh(sourceMesh);

		Geometry geom = new Geometry(objName + "-geom-" + geomIndex++, mesh);

		Material material = null;
		try {
			material = assetManager.loadAsset(new SwitchedMaterialKey(sourceMesh.materialName, materialSwitch));
		} catch (AssetNotFoundException | AssetLoadException e) {
			logger.warning("Unable to load the material '" + sourceMesh.materialName + "': " + e);
		}

		if (material == null) {
			// create default material
			material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
			material.setFloat("Shininess", 64);
		}

		if (material != null) {
			geom.setMaterial(material);
			if (material.isTransparent()) {
				geom.setQueueBucket(Bucket.Transparent);
			} else {
				geom.setQueueBucket(Bucket.Opaque);
			}
		}

		return geom;
	}

	protected Mesh constructMesh(IntermediateMesh sourceMesh) {
		Mesh m = new Mesh();
		m.setMode(Mode.Triangles);

		boolean hasTexCoord = true;
		boolean hasNormals = true;

		FloatBuffer posBuf = BufferUtils.createFloatBuffer(sourceMesh.vertices.size() * 3);
		FloatBuffer normBuf = null;
		FloatBuffer tcBuf = null;

		if (hasNormals) {
			normBuf = BufferUtils.createFloatBuffer(sourceMesh.normals.size() * 3);
			m.setBuffer(VertexBuffer.Type.Normal, 3, normBuf);
		}
		if (hasTexCoord) {
			tcBuf = BufferUtils.createFloatBuffer(sourceMesh.texCoords.size() * 2);
			m.setBuffer(VertexBuffer.Type.TexCoord, 2, tcBuf);
		}

		IndexBuffer indexBuf = null;
		if (sourceMesh.vertices.size() >= 65536) {
			// too many verticies: use intbuffer instead of shortbuffer
			IntBuffer ib = BufferUtils.createIntBuffer(sourceMesh.indices.size());
			m.setBuffer(VertexBuffer.Type.Index, 3, ib);
			indexBuf = new IndexIntBuffer(ib);
		} else {
			ShortBuffer sb = BufferUtils.createShortBuffer(sourceMesh.indices.size());
			m.setBuffer(VertexBuffer.Type.Index, 3, sb);
			indexBuf = new IndexShortBuffer(sb);
		}

		for (int i = 0; i < sourceMesh.vertices.size(); i++) {
			bCVector vertex = sourceMesh.vertices.get(i);
			posBuf.put(vertex.getX()).put(vertex.getY()).put(isLeftHanded() ? vertex.getZ() : -vertex.getZ());

			bCVector normal = sourceMesh.normals.get(i);
			normBuf.put(normal.getX()).put(normal.getY()).put(normal.getZ());

			bCVector2 texture = sourceMesh.texCoords.get(i);
			tcBuf.put(texture.getX()).put(texture.getY());
		}

		for (int i = 0; i < sourceMesh.indices.size(); i += 3) {
			indexBuf.put(i, sourceMesh.indices.get(i));
			if (isLeftHanded()) {
				indexBuf.put(i + 1, sourceMesh.indices.get(i + 1));
				indexBuf.put(i + 2, sourceMesh.indices.get(i + 2));
			} else {
				indexBuf.put(i + 1, sourceMesh.indices.get(i + 2));
				indexBuf.put(i + 2, sourceMesh.indices.get(i + 1));
			}

		}

		m.setBuffer(VertexBuffer.Type.Position, 3, posBuf);
		// index buffer and others were set on creation

		m.setStatic();
		m.updateCounts();
		m.updateBound();
		// m.setInterleaved();

		return m;
	}

	@Override
	public Object load(AssetInfo info) throws IOException {
		logger.info("Loading: " + info.getKey().getName());

		ModelKey key = (ModelKey) info.getKey();
		assetManager = info.getManager();

		objName = key.getName();
		geomIndex = 0;

		String folderName = key.getFolder();
		String ext = key.getExtension();
		objName = objName.substring(0, objName.length() - ext.length() - 1);
		if (folderName != null && folderName.length() > 0) {
			objName = objName.substring(folderName.length());
		}

		Node objNode = new Node(objName + "-" + key.getExtension() + "node");

		if (!(info.getKey() instanceof ModelKey)) {
			throw new IllegalArgumentException("Model assets must be loaded using a ModelKey");
		}

		int materialSwitch = 0;
		if (info.getKey() instanceof SwitchedModelKey) {
			materialSwitch = ((SwitchedModelKey) info.getKey()).getMaterialSwitch();
		}

		try (InputStream is = info.openStream()) {
			for (IntermediateMesh mesh : getMesh(is)) {
				objNode.attachChild(createGeometry(mesh, materialSwitch));
			}
		} catch (IllegalMeshException e) {
			throw new IOException(e);
		}

		if (objNode.getQuantity() == 1) {
			// only 1 geometry, so no need to send node
			return objNode.getChild(0);
		} else {
			return objNode;
		}
	}

	public abstract List<IntermediateMesh> getMesh(InputStream is) throws IOException, IllegalMeshException;

	/**
	 * To port a mesh from DirectX (left-handed coordinate system) to OpenGL (right-handed) do the
	 * following:
	 * <p>
	 * (taken from https://stackoverflow.com/a/16602933)<br>
	 * Direct3D uses a left-handed coordinate system. If you are porting an application that is
	 * based on a right-handed coordinate system, you must make two changes to the data passed to
	 * Direct3D.
	 * <ul>
	 * <li>Flip the order of triangle vertices so that the system traverses them clockwise from the
	 * front. In other words, if the vertices are v0, v1, v2, pass them to Direct3D as v0, v2,
	 * v1.</li>
	 * <li>Use the view matrix to scale world space by -1 in the z-direction. To do this, flip the
	 * sign of the _31, _32, _33, and _34 member of the D3DMATRIX structure that you use for your
	 * view matrix.</li>
	 * </ul>
	 * But somehow if you do this for Gothic 3 meshes (.xcmsh) the lighting/normals is broken. If
	 * you just scale the world space by -1, everything looks fine. It seems like .xcmsh already has
	 * a different vertex order than DirectX, namely the OpenGL vertex order. However animated
	 * meshes (.xact) do not need an inverted z axis and therefore we need to invert its z
	 * coordinates, to negate the globally inverted z axis. In addition they need their vertex order
	 * flipped. You get a mirrored mesh, if you respectively do the other operation for .xcmsh /
	 * .xact. So both of these formats are somehow inconsistent :D
	 *
	 * @return
	 */
	public abstract boolean isLeftHanded();
}
