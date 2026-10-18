package com.blendroid.app.engine.model

data class Bone(
    val id: String,
    val name: String,
    val parentId: String? = null,
    val head: Vector3 = Vector3.Zero,
    val tail: Vector3 = Vector3(0f, 1f, 0f),
    var transform: Transform = Transform.Identity,
    var isSelected: Boolean = false
)

data class Armature(
    val bones: MutableList<Bone> = mutableListOf()
) {
    fun findBone(id: String): Bone? = bones.find { it.id == id }

    companion object {
        fun createHumanoidRig(): Armature {
            val root = Bone("b_root", "Root", null, Vector3(0f, 0f, 0f), Vector3(0f, 0.8f, 0f))
            val spine = Bone("b_spine", "Spine", "b_root", Vector3(0f, 0.8f, 0f), Vector3(0f, 1.4f, 0f))
            val head = Bone("b_head", "Head", "b_spine", Vector3(0f, 1.4f, 0f), Vector3(0f, 1.8f, 0f))
            val leftArm = Bone("b_l_arm", "Arm_L", "b_spine", Vector3(-0.3f, 1.3f, 0f), Vector3(-0.9f, 1.3f, 0f))
            val rightArm = Bone("b_r_arm", "Arm_R", "b_spine", Vector3(0.3f, 1.3f, 0f), Vector3(0.9f, 1.3f, 0f))
            val leftLeg = Bone("b_l_leg", "Leg_L", "b_root", Vector3(-0.3f, 0.8f, 0f), Vector3(-0.3f, 0f, 0f))
            val rightLeg = Bone("b_r_leg", "Leg_R", "b_root", Vector3(0.3f, 0.8f, 0f), Vector3(0.3f, 0f, 0f))

            return Armature(mutableListOf(root, spine, head, leftArm, rightArm, leftLeg, rightLeg))
        }
    }
}

data class Keyframe(
    val frame: Int,
    val transform: Transform = Transform.Identity,
    val boneTransforms: Map<String, Transform> = emptyMap()
)

data class AnimationTrack(
    val name: String = "Action",
    val keyframes: MutableList<Keyframe> = mutableListOf(),
    val totalFrames: Int = 250,
    val fps: Int = 24
) {
    fun evaluateTransformAt(frame: Int): Transform {
        if (keyframes.isEmpty()) return Transform.Identity
        val sorted = keyframes.sortedBy { it.frame }

        if (frame <= sorted.first().frame) return sorted.first().transform
        if (frame >= sorted.last().frame) return sorted.last().transform

        for (i in 0 until sorted.size - 1) {
            val k1 = sorted[i]
            val k2 = sorted[i + 1]
            if (frame in k1.frame..k2.frame) {
                val span = (k2.frame - k1.frame).toFloat()
                val t = if (span > 0) (frame - k1.frame) / span else 0f

                val interpolatedPos = Vector3.lerp(k1.transform.position, k2.transform.position, t)
                val interpolatedRot = Vector3.lerp(k1.transform.rotation, k2.transform.rotation, t)
                val interpolatedScale = Vector3.lerp(k1.transform.scale, k2.transform.scale, t)

                return Transform(interpolatedPos, interpolatedRot, interpolatedScale)
            }
        }

        return sorted.last().transform
    }

    fun evaluateBoneTransformAt(boneId: String, frame: Int): Transform {
        if (keyframes.isEmpty()) return Transform.Identity
        val sorted = keyframes.sortedBy { it.frame }

        val k1 = sorted.lastOrNull { it.frame <= frame } ?: sorted.first()
        val k2 = sorted.firstOrNull { it.frame >= frame } ?: sorted.last()

        val t1 = k1.boneTransforms[boneId] ?: Transform.Identity
        val t2 = k2.boneTransforms[boneId] ?: Transform.Identity

        if (k1.frame == k2.frame) return t1

        val span = (k2.frame - k1.frame).toFloat()
        val t = if (span > 0) (frame - k1.frame) / span else 0f

        return Transform(
            position = Vector3.lerp(t1.position, t2.position, t),
            rotation = Vector3.lerp(t1.rotation, t2.rotation, t),
            scale = Vector3.lerp(t1.scale, t2.scale, t)
        )
    }
}
