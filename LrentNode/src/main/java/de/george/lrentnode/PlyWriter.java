package de.george.lrentnode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import com.google.common.base.Joiner;

import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;
import de.george.lrentnode.classes.eCResourceMeshComplex_PS;
import de.george.lrentnode.enums.G3Enums.eEVertexStreamArrayType;
import de.george.lrentnode.structures.eCMeshElement;

public class PlyWriter {
	private Writer writer;

	private void lines(String... lines) throws IOException {
		for (String line : lines) {
			writer.append(line).append("\n");
		}
	}

	private void join(String... parts) throws IOException {
		writer.append(Joiner.on(" ").join(parts)).append("\n");
	}

	private void format(String format, Object... args) throws IOException {
		writer.append(String.format(format, args)).append("\n");
	}

	public void write(eCResourceMeshComplex_PS meshComplex, OutputStreamWriter out) throws IOException {
		writer = new BufferedWriter(out);
		for (eCMeshElement mesh : meshComplex.meshElements) {
			writeMeshElement(mesh);
		}
		writer.flush();
	}

	private void writeMeshElement(eCMeshElement mesh) throws IOException {
		List<Integer> faces = mesh.getStreamArrayByType(eEVertexStreamArrayType.eEVertexStreamArrayType_Face);
		List<bCVector> vertices = mesh.getStreamArrayByType(eEVertexStreamArrayType.eEVertexStreamArrayType_VertexPosition);
		List<bCVector> normals = mesh.getStreamArrayByType(eEVertexStreamArrayType.eEVertexStreamArrayType_Normal);
		List<bCVector2> uvs = mesh.getStreamArrayByType(eEVertexStreamArrayType.eEVertexStreamArrayType_TextureCoordinate);
		List<Integer> colors = mesh.getStreamArrayByType(eEVertexStreamArrayType.eEVertexStreamArrayType_Diffuse);

		lines("ply", "format ascii 1.0", "comment Created by g3dit");
		format("comment Material: %s", mesh.getMaterialName());
		format("comment BoundingBox: %s", mesh.getBoundingBox());
		format("comment FVF: %s", mesh.getFvf());
		format("element vertex %d", vertices.size());
		lines("property float x", "property float y", "property float z");
		lines("property float nx", "property float ny", "property float nz");
		lines("property float s", "property float t");
		lines("property uchar red", "property uchar green", "property uchar blue");
		format("element face %d", faces.size() / 3);
		lines("property list uchar uint vertex_indices", "end_header");

		for (int i = 0; i < vertices.size(); i++) {
			bCVector vertex = vertices.get(i);
			bCVector normal = normals.get(i);
			bCVector2 uv = uvs.get(i);
			Integer color = colors.get(i);
			format("%.6f %.6f %.6f %.6f %.6f %.6f %.6f %.6f %d %d %d", vertex.getX(), vertex.getY(), vertex.getZ(), normal.getX(),
					normal.getY(), normal.getZ(), uv.getX(), uv.getY(), color >> 24 & 0xff, color >> 24 & 0xff, color >> 24 & 0xff);
		}

		for (int i = 0; i < faces.size(); i += 3) {
			format("3 %d %d %d", faces.get(i), faces.get(i + 1), faces.get(i + 2));
		}
	}
}
