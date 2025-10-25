package org.drakum.anim;

import java.io.InputStream;
import java.net.URL;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drakum.Shader;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.assimp.AIQuatKey;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVectorKey;
import org.lwjgl.assimp.AIVertexWeight;
import org.lwjgl.assimp.Assimp;

public class AssimpLoader
{
	public static AnimatedModel load(String file)
	{
		try
		{
			URL url = Shader.class.getResource(file);

			Path temp = Files.createTempFile("model_", ".dae");
			try (InputStream in = url.openStream())
			{
				Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
			}

			AIScene scene = Assimp.aiImportFile(temp.toAbsolutePath().toString(), Assimp.aiProcess_Triangulate | Assimp.aiProcess_JoinIdenticalVertices);
			Files.deleteIfExists(temp);

			if (scene == null) throw new RuntimeException("Assimp error: " + Assimp.aiGetErrorString());

			Map<String, AIBone> boneMap = new HashMap<>();

			// Ausgabe: Bones pro Mesh
			for (int i = 0; i < scene.mNumMeshes(); i++)
			{
				AIMesh mesh = AIMesh.create(scene.mMeshes().get(i));

				PointerBuffer bones = mesh.mBones();
				for (int b = 0; b < mesh.mNumBones(); b++)
				{
					AIBone bone = AIBone.create(bones.get(b));
					boneMap.put(bone.mName().dataString(), bone);
				}

			}

			Node root = buildNodeTree(scene.mRootNode(), boneMap);

			// Eigene Bone-Laufzeitstrukturen
			List<Bone> bones = new ArrayList<>();
			Map<String, Bone> boneByName = new HashMap<>();

			int boneIndex = 0;
			for (AIBone aiBone : boneMap.values())
			{
				String name = aiBone.mName().dataString();
				Matrix4f offset = toMatrix(aiBone.mOffsetMatrix());
				Node node = findNode(root, name);
				Bone bone = new Bone(name, boneIndex++, offset, node);
				bones.add(bone);
				boneByName.put(name, bone);
			}

			List<Animation> anims = parseAnimations(scene);
			List<SkinnedMesh> meshes = loadMeshes(scene, boneByName);

			// GPU-Upload
			for (SkinnedMesh mesh : meshes)
				mesh.uploadToGPU(mesh.positions, mesh.normals, mesh.texcoords, mesh.boneIds, mesh.weights, mesh.indices);

			return new AnimatedModel(root, bones, boneByName, anims, meshes);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static Node buildNodeTree(AINode aiNode, Map<String, AIBone> boneMap)
	{
		Matrix4f transform = toMatrix(aiNode.mTransformation());
		Node node = new Node(aiNode.mName().dataString(), transform);

		// falls dieser Node ein Bone ist
		node.bone = boneMap.get(node.name);

		PointerBuffer children = aiNode.mChildren();
		for (int i = 0; i < aiNode.mNumChildren(); i++)
		{
			AINode child = AINode.create(children.get(i));
			node.children.add(buildNodeTree(child, boneMap));
		}

		return node;
	}

	private static Matrix4f toMatrix(AIMatrix4x4 m)
	{
		Matrix4f mat = new Matrix4f();
		mat.m00(m.a1());
		mat.m01(m.a2());
		mat.m02(m.a3());
		mat.m03(m.a4());
		mat.m10(m.b1());
		mat.m11(m.b2());
		mat.m12(m.b3());
		mat.m13(m.b4());
		mat.m20(m.c1());
		mat.m21(m.c2());
		mat.m22(m.c3());
		mat.m23(m.c4());
		mat.m30(m.d1());
		mat.m31(m.d2());
		mat.m32(m.d3());
		mat.m33(m.d4());
		mat.transpose();
		return mat;
	}
	
	public static List<Animation> parseAnimations(AIScene scene)
	{
		List<Animation> animations = new ArrayList<>();

		for (int i = 0; i < scene.mNumAnimations(); i++)
		{
			AIAnimation aiAnim = AIAnimation.create(scene.mAnimations().get(i));
			String name = aiAnim.mName().dataString();

			float duration = (float) aiAnim.mDuration();
			float ticksPerSecond = aiAnim.mTicksPerSecond() != 0 ? (float) aiAnim.mTicksPerSecond() : 20f;

			Animation anim = new Animation(name, duration, ticksPerSecond);

			PointerBuffer channels = aiAnim.mChannels();
			for (int c = 0; c < aiAnim.mNumChannels(); c++)
			{
				AINodeAnim channel = AINodeAnim.create(channels.get(c));
				BoneTrack track = new BoneTrack(channel.mNodeName().dataString());

				// Positions
				for (int k = 0; k < channel.mNumPositionKeys(); k++)
				{
					AIVectorKey key = channel.mPositionKeys().get(k);
					Vector3f pos = new Vector3f(key.mValue().x(), key.mValue().y(), key.mValue().z());
					ensureTrackKey(track, key.mTime()).position.set(pos);
				}

				// Rotations
				for (int k = 0; k < channel.mNumRotationKeys(); k++)
				{
					AIQuatKey key = channel.mRotationKeys().get(k);
					Quaternionf rot = new Quaternionf(key.mValue().x(), key.mValue().y(), key.mValue().z(), key.mValue().w());
					ensureTrackKey(track, key.mTime()).rotation.set(rot);
				}

				// Scales
				for (int k = 0; k < channel.mNumScalingKeys(); k++)
				{
					AIVectorKey key = channel.mScalingKeys().get(k);
					Vector3f sca = new Vector3f(key.mValue().x(), key.mValue().y(), key.mValue().z());
					ensureTrackKey(track, key.mTime()).scale.set(sca);
				}

				anim.tracks.add(track);
			}

			animations.add(anim);
		}

		return animations;
	}

	private static BoneKey ensureTrackKey(BoneTrack track, double time)
	{
		float t = (float) time;
		
		for (BoneKey k : track.keys)
		{
			if (Math.abs(k.time - t) < 1e-6f) return k;
		}
		
		BoneKey k = new BoneKey(t, new Vector3f(), new Quaternionf(), new Vector3f(1, 1, 1));
		
		track.keys.add(k);
		
		return k;
	}

	public static Node findNode(Node node, String name)
	{
		if (node.name.equals(name)) return node;
		
		for (Node c : node.children)
		{
			Node res = findNode(c, name);
			
			if (res != null) return res;
		}
		
		return null;
	}

	public static List<SkinnedMesh> loadMeshes(AIScene scene, Map<String, Bone> boneByName)
	{
		List<SkinnedMesh> meshes = new ArrayList<>();

		for (int i = 0; i < scene.mNumMeshes(); i++)
		{
			AIMesh aiMesh = AIMesh.create(scene.mMeshes().get(i));

			int vertexCount = aiMesh.mNumVertices();
			float[] positions = new float[vertexCount * 3];
			float[] normals = new float[vertexCount * 3];
			float[] texcoords = new float[vertexCount * 2];

			// --- Positions
			AIVector3D.Buffer posBuf = aiMesh.mVertices();
			for (int v = 0; v < vertexCount; v++)
			{
				AIVector3D p = posBuf.get(v);
				positions[v * 3 + 0] = p.x();
				positions[v * 3 + 1] = p.y();
				positions[v * 3 + 2] = p.z();
			}

			// --- Normals
			if (aiMesh.mNormals() != null)
			{
				AIVector3D.Buffer nBuf = aiMesh.mNormals();
				for (int v = 0; v < vertexCount; v++)
				{
					AIVector3D n = nBuf.get(v);
					normals[v * 3 + 0] = n.x();
					normals[v * 3 + 1] = n.y();
					normals[v * 3 + 2] = n.z();
				}
			}

			// --- Texcoords
			if (aiMesh.mTextureCoords(0) != null)
			{
				AIVector3D.Buffer tBuf = aiMesh.mTextureCoords(0);
				for (int v = 0; v < vertexCount; v++)
				{
					AIVector3D t = tBuf.get(v);
					texcoords[v * 2 + 0] = t.x();
					texcoords[v * 2 + 1] = t.y();
				}
			}

			// --- Indices
			AIFace.Buffer faces = aiMesh.mFaces();
			int totalIndices = 0;
			for (int s = 0; s < aiMesh.mNumFaces(); s++)
			{
				totalIndices += faces.get(s).mNumIndices();
			}

			int[] indices = new int[totalIndices];
			int p = 0;
			for (int f = 0; f < aiMesh.mNumFaces(); f++)
			{
				AIFace face = faces.get(f);
				IntBuffer indBuf = face.mIndices();
				for (int j = 0; j < face.mNumIndices(); j++)
				{
					indices[p++] = indBuf.get(j);
				}
			}

			// --- Bone influences
			int[] boneIds = new int[vertexCount * 4];
			float[] weights = new float[vertexCount * 4];
			fillBoneData(aiMesh, boneIds, weights, boneByName);

			// jetzt normalisieren:
			for (int v = 0; v < vertexCount; v++)
			{
				int base = v * 4;
				float sum = weights[base] + weights[base + 1] + weights[base + 2] + weights[base + 3];
				if (sum > 0.0f)
				{
					float inv = 1.0f / sum;
					weights[base] *= inv;
					weights[base + 1] *= inv;
					weights[base + 2] *= inv;
					weights[base + 3] *= inv;
				}
			}

			SkinnedMesh mesh = new SkinnedMesh(positions, normals, texcoords, indices, boneIds, weights, vertexCount);
			
			meshes.add(mesh);
		}
		
		return meshes;
	}

	private static void fillBoneData(AIMesh aiMesh, int[] boneIds, float[] weights, Map<String, Bone> boneByName)
	{
		PointerBuffer bonePtrs = aiMesh.mBones();
		for (int b = 0; b < aiMesh.mNumBones(); b++)
		{
			AIBone aiBone = AIBone.create(bonePtrs.get(b));
			Bone bone = boneByName.get(aiBone.mName().dataString());
			if (bone == null) continue; // Bone existiert evtl. nur für andere Meshes

			AIVertexWeight.Buffer vwBuf = aiBone.mWeights();
			for (int w = 0; w < aiBone.mNumWeights(); w++)
			{
				AIVertexWeight vw = vwBuf.get(w);
				int vertexId = vw.mVertexId();
				float weight = vw.mWeight();
				addBoneInfluence(boneIds, weights, vertexId, bone.index, weight);
			}
		}
	}

	private static void addBoneInfluence(int[] boneIds, float[] weights, int vertexId, int boneIndex, float weight)
	{
		int base = vertexId * 4;
		for (int i = 0; i < 4; i++)
		{
			int idx = base + i;
			if (weights[idx] == 0.0f)
			{
				boneIds[idx] = boneIndex;
				weights[idx] = weight;
				return;
			}
		}
		// Falls ein Vertex mehr als 4 Bones hat → Gewicht ignorieren oder normalisieren
	}
}
