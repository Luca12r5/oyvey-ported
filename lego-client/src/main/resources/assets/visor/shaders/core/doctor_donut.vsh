#version 330

// Linear blend skinning: every vertex follows up to four bones of the current pose.

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

layout(std140) uniform DonutData {
    mat4 Bones[64];
    vec4 Light;  // x = block light, y = sky light (0-15), z = 1 to use the lightmap, w unused
    vec4 Glow;   // rgb = colour of the rim light while the pointer is on him, a = its strength
};

uniform sampler2D Sampler2;

in vec3 Position;
in vec2 UV0;
in vec3 BindNormal;
in vec4 Joints;
in vec4 Weights;

out vec2 vUv;
out vec3 vNormal;
out vec4 vLight;
out vec3 vViewPosition;
out vec3 vViewNormal;
out vec4 vGlow;

void main() {
    mat4 skin = Bones[int(Joints.x + 0.5)] * Weights.x
        + Bones[int(Joints.y + 0.5)] * Weights.y
        + Bones[int(Joints.z + 0.5)] * Weights.z
        + Bones[int(Joints.w + 0.5)] * Weights.w;
    vec4 viewPosition = ModelViewMat * (skin * vec4(Position, 1.0));
    gl_Position = ProjMat * viewPosition;
    vNormal = mat3(skin) * BindNormal;
    vViewPosition = viewPosition.xyz;
    vViewNormal = mat3(ModelViewMat) * vNormal;
    vUv = UV0;
    vLight = Light.z > 0.5 ? texelFetch(Sampler2, ivec2(Light.xy), 0) : vec4(1.0);
    vGlow = Glow;
}
