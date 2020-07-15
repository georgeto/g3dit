package de.george.g3dit.jme.asset;

import java.util.ArrayList;
import java.util.List;

import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;

public class IntermediateMesh {
	public String materialName;
	public List<bCVector> vertices;
	public List<bCVector> normals;
	public List<bCVector2> texCoords;
	public List<Integer> indices;

	public IntermediateMesh() {
		vertices = new ArrayList<>();
		normals = new ArrayList<>();
		texCoords = new ArrayList<>();
		indices = new ArrayList<>();
	}

	public void addVertex(bCVector vertex) {
		vertices.add(vertex);
	}

	public void addNormal(bCVector normal) {
		normals.add(normal);
	}

	public void addTexCoord(bCVector2 texCoord) {
		texCoords.add(texCoord);
	}
}
