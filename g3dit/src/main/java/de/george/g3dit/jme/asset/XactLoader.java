package de.george.g3dit.jme.asset;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.george.g3dit.jme.asset.MeshUtil.IllegalMeshException;
import de.george.lrentnode.util.FileUtil;

public class XactLoader extends AbstractG3MeshLoader {

	@Override
	public List<IntermediateMesh> getMesh(InputStream is) throws IOException, IllegalMeshException {
		return MeshUtil.toIntermediateMesh(FileUtil.openAnimationActor(is));
	}

	@Override
	public boolean isLeftHanded() {
		return false;
	}
}
